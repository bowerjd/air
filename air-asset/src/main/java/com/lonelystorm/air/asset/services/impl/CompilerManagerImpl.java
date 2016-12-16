package com.lonelystorm.air.asset.services.impl;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.lonelystorm.air.asset.exceptions.CompilerException;
import com.lonelystorm.air.asset.models.Asset;
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.models.AssetThemeConfiguration;
import com.lonelystorm.air.asset.services.CacheManager;
import com.lonelystorm.air.asset.services.Compiler;
import com.lonelystorm.air.asset.services.CompilerManager;
import com.lonelystorm.air.asset.services.LibraryResolver;
import com.lonelystorm.air.asset.services.ThemeConfigurationService;
import com.lonelystorm.air.asset.util.CompilerSession;

@Component
@Service
public class CompilerManagerImpl implements CompilerManager {

    @Reference(
        referenceInterface = Compiler.class,
        policy = ReferencePolicy.DYNAMIC,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        bind = "bindCompiler",
        unbind = "unbindCompiler"
    )
    private final Map<String, Compiler> compilers = Collections.synchronizedMap(new TreeMap<String, Compiler>());

    @Reference(
            referenceInterface = ThemeConfigurationService.class,
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
            bind = "bindThemeConfigService",
            unbind = "unbindThemeConfigService"
        )
    private final Map<String, ThemeConfigurationService> themeConfigurators = Collections.synchronizedMap(new TreeMap<String, ThemeConfigurationService>());

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private CacheManager cacheManager;

    @Reference
    private LibraryResolver libraryResolver;

    private ListeningExecutorService pool;

    private BiMap<String, ListenableFuture<String>> tasks;

    @Activate
    protected void activate(ComponentContext componentContext) {
        pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
        tasks = Maps.synchronizedBiMap(HashBiMap.<String, ListenableFuture<String>>create(30));
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (pool != null) {
            pool.shutdown();
        }

        pool = null;
        tasks = null;
    }

    protected void bindCompiler(Compiler compiler) {
        String name = compiler.getClass().getName();
        compilers.put(name, compiler);
    }

    protected void unbindCompiler(Compiler compiler) {
        String name = compiler.getClass().getName();
        compilers.remove(name);
    }

    protected void bindThemeConfigService(ThemeConfigurationService service) {
        themeConfigurators.put(service.getClass().getName(), service);
    }

    protected void  unbindThemeConfigService(ThemeConfigurationService service) {
        themeConfigurators.remove(service.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String compile(Asset asset) throws CompilerException {
        final StringBuilder compiled = new StringBuilder();
        final List<ListenableFuture<String>> futures = new ArrayList<>();

        compile(asset, futures);

        ListenableFuture<List<String>> all = Futures.allAsList(futures);
        try {
            all.get();

            for (ListenableFuture<String> future : futures) {
                String path = tasks.inverse().get(future);
                String result = future.get();

                cacheManager.cache(path, result);
                compiled.append(result);
            }
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof CompilerException) {
                throw (CompilerException) e.getCause();
            }
        } finally {
            for (ListenableFuture<String> future : futures) {
                synchronized (tasks) {
                    tasks.inverse().remove(future);
                }
            }
        }

        return compiled.toString();
    }

    private void compile(final Asset asset, final List<ListenableFuture<String>> futures) {
        if (asset instanceof AssetLibrary) {
            AssetLibrary folder = (AssetLibrary) asset;
            for (final String embed : folder.getEmbed()) {
                List<AssetLibrary> libs = libraryResolver.findLibrariesByCategory(embed);
                for (final AssetLibrary lib : libs) {
                    compile(lib, futures);
                }
            }
        } else if (asset instanceof AssetTheme) {
            AssetTheme folder = (AssetTheme) asset;
            for (final String embed : folder.getEmbed()) {
                Collection<AssetTheme> themes = libraryResolver.findThemesByTheme(embed);
                for (final AssetTheme theme : themes) {
                    compile(theme, futures);
                }
            }
        } else if (asset instanceof AssetThemeConfiguration) {
            final AssetThemeConfiguration conf = (AssetThemeConfiguration)asset;
            for (final String embed : conf.getEmbed()) {
                final Collection<AssetTheme> themes = libraryResolver.findThemesByTheme(embed);
                for (final AssetTheme theme : themes) {
                    compile(theme, futures);
                }
            }
        }

        if (asset instanceof AssetThemeConfiguration) {
            final AssetThemeConfiguration conf = (AssetThemeConfiguration)asset;
            final String result = cacheManager.get(conf.getRenderLocationPath());
            ListenableFuture<String> future = null;

            synchronized (tasks) {
                if (tasks.containsKey(conf.getRenderLocationPath())) {
                    future = tasks.get(conf.getRenderLocationPath());
                } else {
                    if (result == null) {
                        future = compile(conf);
                    } else {
                        future = Futures.immediateFuture(result);
                    }

                    if (future != null) {
                        tasks.put(conf.getRenderLocationPath(), future);
                    }
                }
            }

            if (future != null) {
                futures.add(future);
            }

        } else {
            for (final String file : asset.getSources()) {
                ListenableFuture<String> future = null;
                final String result = cacheManager.get(file);

                synchronized (tasks) {
                    if (tasks.containsKey(file)) {
                        future = tasks.get(file);
                    } else {
                        if (result == null) {
                            future = compile(asset, file);
                        } else {
                            future = Futures.immediateFuture(result);
                        }

                        if (future != null) {
                            tasks.put(file, future);
                        }
                    }
                }

                if (future != null) {
                    futures.add(future);
                }
            }
        }
    }

    private ListenableFuture<String> compile(AssetThemeConfiguration config) {
        final CompilerSession session = new CompilerSession(resourceResolverFactory, getClass());

        String source = session.file(config.getThemeSource());

        if (source != null) {
            for (Compiler compiler : compilers.values()) {
                if (compiler.supports(config.getBaseTheme(), config.getThemeSource())) {
                    boolean configured = false;
                    // Augment the source
                    for (ThemeConfigurationService configurator : themeConfigurators.values()) {
                        if (configurator.supports(config)) {
                            configured = true;
                            final String originalSource = source;
                            source = configurator.augmentTheme(config, source);
                            if (LOG.isDebugEnabled()) {
                                if (LOG.isTraceEnabled()) {
                                    LOG.trace(format("Augmenting %s with %s by %s:%n"+
                                           ">>>>>>>>>>>>>>>>Orignal Source<<<<<<<<<<<<<<%n%s%n"+
                                           "}}}}}}}}}}}}}}}}New Source{{{{{{{{{{{{{{{{{{%n%s%n===========================================",
                                           config.getBaseTheme().getPath(), config.getPath(), configurator.getClass().getName(), originalSource, source));
                                } else {
                                    LOG.debug("Augmenting {} with {} by {}", config.getBaseTheme().getPath(), config.getPath(), configurator.getClass().getName());
                                }
                            }
                            break;
                        }
                    }

                    if (!configured) {
                        LOG.warn("Failed to find a supported {} to augment {}", ThemeConfigurationService.class.getSimpleName(), config);
                    }

                    return pool.submit(new CompileTask(compiler, config.getBaseTheme(), config.getRenderLocationPath(), source));
                }
            }
        }

        return null;
    }

    private ListenableFuture<String> compile(final Asset asset, final String file) {
        final CompilerSession session = new CompilerSession(resourceResolverFactory, getClass());
        final String source = session.file(file);

        if (source != null) {
            for (Compiler compiler : compilers.values()) {
                if (compiler.supports(asset, file)) {
                    return pool.submit(new CompileTask(compiler, asset, file, source));
                }
            }
        }

        return null;
    }

    private static class CompileTask implements Callable<String> {

        private final Compiler compiler;

        private final Asset asset;

        private final String file;

        private final String source;

        public CompileTask(final Compiler compiler, final Asset asset, final String file, final String source) {
            this.compiler = compiler;
            this.asset = asset;
            this.file = file;
            this.source = source;
        }

        @Override
        public String call() throws CompilerException {
            return compiler.compile(asset, file, source);
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(CompilerManagerImpl.class);
}
