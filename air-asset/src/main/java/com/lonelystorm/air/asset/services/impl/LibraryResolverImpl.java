package com.lonelystorm.air.asset.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.ComponentContext;

import com.lonelystorm.air.asset.models.Asset;
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.models.AssetThemeConfiguration;
import com.lonelystorm.air.asset.services.LibraryAdapterManager;
import com.lonelystorm.air.asset.services.LibraryResolver;
import com.lonelystorm.air.util.EscalatedResolver;

@Component
@Service
public class LibraryResolverImpl implements LibraryResolver {

    @Reference
    private LibraryAdapterManager libraryAdapterManager;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private volatile Map<String, AssetLibrary> libraries;

    private volatile Map<String, AssetLibrary> sources;

    private volatile Map<String, List<AssetLibrary>> categories;

    private volatile Map<String, AssetTheme> themes;

    private volatile Map<String, AssetThemeConfiguration> themeConfigs;
    
    private volatile Map<String, Set<AssetTheme>> themesCategories;


    @Activate
    public void activate(ComponentContext context) {
        libraries = new TreeMap<>();
        sources = new TreeMap<>();
        categories = new TreeMap<>();

        themes = new TreeMap<>();
        themeConfigs = new TreeMap<>();
        themesCategories = new TreeMap<>();
    }

    @Override
    public Asset load(final String path) {
        EscalatedResolver escalated = new EscalatedResolver(resourceResolverFactory, getClass());
        AssetLibrary library = escalated.doSession(new EscalatedResolver.Session<AssetLibrary>() {

            @Override
            public AssetLibrary run(ResourceResolver resolver) {
                Resource resource = resolver.getResource(path);
                if (resource != null) {
                    AssetLibrary library = libraryAdapterManager.library(resource);
                    return library;
                }

                return null;
            }

        });

        if (library != null) {
            add(library);
        }

        return library;
    }

    @Override
    public AssetThemeConfiguration loadThemeConfiguration(final String path) {
        EscalatedResolver escalated = new EscalatedResolver(resourceResolverFactory, getClass());
        AssetThemeConfiguration themeConf = escalated.doSession(new EscalatedResolver.Session<AssetThemeConfiguration>() {

            @Override
            public AssetThemeConfiguration run(ResourceResolver resolver) {
                Resource resource = resolver.getResource(path);
                if (resource != null) {
                    AssetThemeConfiguration themeConf = libraryAdapterManager.themeConfiguration(resource);
                    return themeConf;
                }

                return null;
            }

        });

        if (themeConf != null) {
            addThemeConfiguration(themeConf);
        }

        return themeConf;
    }

    
    @Override
    public void clear() {
        synchronized (this) {
            libraries.clear();
            sources.clear();
            categories.clear();

            themes.clear();
            themeConfigs.clear();
        }
    }

    @Override
    public void add(AssetLibrary library) {
        synchronized (this) {
            libraries.put(library.getPath(), library);

            for (String source : library.getSources()) {
                sources.put(source, library);
            }

            for (String category : library.getCategories()) {
                if (!categories.containsKey(category)) {
                    categories.put(category, new ArrayList<AssetLibrary>());
                }
                categories.get(category).add(library);
            }

            for (AssetTheme theme : library.getThemes()) {
                themes.put(theme.getPath(), theme);

                for (String category : theme.getThemes()) {
                    if (!themesCategories.containsKey(category)) {
                        themesCategories.put(category, new HashSet<AssetTheme>());
                    }
                    themesCategories.get(category).add(theme);
                }
            }
        }
    }

    @Override
    public void addThemeConfiguration(AssetThemeConfiguration themeConfig) {
        synchronized (this) {
            themeConfigs.put(themeConfig.getPath(), themeConfig);
        }
    }

    @Override
    public Collection<AssetLibrary> findAllLibraries() {
        return libraries.values();
    }

    @Override
    public AssetLibrary findLibraryBySource(String source) {
        synchronized (this) {
            return sources.get(source);
        }
    }

    @Override
    public AssetLibrary findLibraryByPath(String path) {
        synchronized (this) {
            AssetLibrary library = null;

            library = findLibraryBySource(path);
            if (library == null) {
                library = libraries.get(path);
            }

            return library;
        }
    }

    @Override
    public List<AssetLibrary> findLibrariesByCategory(String category) {
        synchronized (this) {
            return categories.get(category);
        }
    }

    @Override
    public Collection<AssetTheme> findAllThemes() {
        return themes.values();
    }

    @Override
    public AssetTheme findThemeByPath(String path) {
        synchronized (this) {
            return themes.get(path);
        }
    }
    
    @Override
    public AssetThemeConfiguration findThemeConfigurationByPath(String path) {
        synchronized (this) {
            return themeConfigs.get(path);
        }    
    }

    @Override
    public Collection<AssetThemeConfiguration> findAllThemeConfigurations() {
        return themeConfigs.values();
    }

    @Override
    public Set<AssetTheme> findThemesByTheme(String theme) {
        synchronized (this) {
            return themesCategories.get(theme);
        }
    }

}
