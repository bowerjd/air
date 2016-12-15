package com.lonelystorm.air.asset.services.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;

import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.models.AssetThemeConfiguration;
import com.lonelystorm.air.asset.services.LibraryAdapterManager;

/**
 * The LibraryAdapterManagerImpl provides the implementation
 * for the LibraryAdapterManager interface.
 *
 * The service requires the reference to both AdaptorFactories for
 * AssetLibrary and AssetTheme to resolve the race condition.
 */
@Component
@Service
public class LibraryAdapterManagerImpl implements LibraryAdapterManager {

    /**
     * AssetLibrary Adaptor
     */
    @Reference(target = "(models.adapter.implementationClass=com.lonelystorm.air.asset.models.AssetLibrary)")
    private AdapterFactory libraryAdaptorFactory;

    /**
     * AssetTheme Adaptor
     */
    @Reference(target = "(models.adapter.implementationClass=com.lonelystorm.air.asset.models.AssetTheme)")
    private AdapterFactory themeAdaptorFactory;
    

    /**
     * AssetTheme Configuration Adaptor
     */
    @Reference(target = "(models.adapter.implementationClass=com.lonelystorm.air.asset.models.AssetThemeConfiguration)")
    private AdapterFactory themeConfigAdaptorFactory;
    

    /**
     * {@inheritDoc}
     */
    @Override
    public AssetLibrary library(Resource resource) {
        return libraryAdaptorFactory.getAdapter(resource, AssetLibrary.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AssetTheme theme(Resource resource) {
        return themeAdaptorFactory.getAdapter(resource, AssetTheme.class);
    }

    @Override
    public AssetThemeConfiguration themeConfiguration(Resource resource) {
        return themeConfigAdaptorFactory.getAdapter(resource, AssetThemeConfiguration.class);
    }
}
