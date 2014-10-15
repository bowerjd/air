package com.lonelystorm.air.asset.services.impl;

import static com.lonelystorm.air.asset.util.PropertiesUtil.comparePropertyValue;
import static com.lonelystorm.air.asset.util.PropertiesUtil.containsProperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import com.lonelystorm.air.asset.models.Asset;
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.services.CacheManager;
import com.lonelystorm.air.asset.services.Compiler;
import com.lonelystorm.air.asset.services.CompilerManager;
import com.lonelystorm.air.asset.services.LibraryResolver;
import com.lonelystorm.air.util.EscalatedResolver;

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
    private Map<String, Compiler> compilers = Collections.synchronizedMap(new TreeMap<String, Compiler>());

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private CacheManager cacheManager;

    @Reference
    private LibraryResolver libraryResolver;

    protected void bindCompiler(Compiler compiler) {
        String name = compiler.getClass().getName();
        compilers.put(name, compiler);
    }

    protected void unbindCompiler(Compiler compiler) {
        String name = compiler.getClass().getName();
        compilers.remove(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String compile(Asset library) {
        final StringBuilder compiled = new StringBuilder();

        compile(library, compiled);

        return compiled.toString();
    }

    private void compile(Asset library, StringBuilder compiled) {
        if (library instanceof AssetLibrary) {
            AssetLibrary folder = (AssetLibrary) library;
            for (String embed : folder.getEmbed()) {
                List<AssetLibrary> libs = libraryResolver.findLibrariesByCategory(embed);
                for (AssetLibrary lib : libs) {
                    compile(lib, compiled);
                }
            }
        } else if (library instanceof AssetTheme) {
            AssetTheme folder = (AssetTheme) library;
            for (String embed : folder.getEmbed()) {
                List<AssetTheme> themes = libraryResolver.findThemesByTheme(embed);
                for (AssetTheme theme : themes) {
                    compile(theme, compiled);
                }
            }
        }

        for (String file : library.getSources()) {
            String result = cacheManager.get(file);
            if (result == null) {
                result = compile(library, file);
                if (result != null) {
                    cacheManager.cache(file, result);
                }
            }

            if (result != null) {
                compiled.append(result);
            }
        }
    }

    private String compile(final Asset library, final String file) {
        final EscalatedResolver escalated = new EscalatedResolver(resourceResolverFactory, getClass());
        final String source = escalated.doSession(new EscalatedResolver.Session<String>() {

            @Override
            public String run(ResourceResolver resolver) {
                Resource resource = resolver.getResource(String.format("%s/jcr:content", file));
                ValueMap properties = ResourceUtil.getValueMap(resource);

                if (comparePropertyValue(properties, "jcr:primaryType", "nt:resource") && containsProperty(properties, "jcr:data")) {
                    InputStream is = properties.get("jcr:data", InputStream.class);
                    String source = null;

                    try {
                        source = IOUtils.toString(is);
                    } catch (IOException e) {
                        // TODO: Log me
                    } finally {
                        IOUtils.closeQuietly(is);
                    }

                    return source;
                }

                return null;
            }

        });

        if (source != null) {
            for (Compiler compiler : compilers.values()) {
                if (compiler.supports(library, file)) {
                    return compiler.compile(library, file, source);
                }
            }
        }

        return null;
    }

}
