package com.lonelystorm.air.asset.services.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;

import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.services.LibraryAdapterManager;

@Component
@Service
public class LibraryAdapterManagerImpl implements LibraryAdapterManager {

    @Reference(target = "(models.adapter.implementationClass=com.lonelystorm.air.asset.models.AssetLibrary)")
    private AdapterFactory libraryAdaptorFactory;

    @Reference(target = "(models.adapter.implementationClass=com.lonelystorm.air.asset.models.AssetTheme)")
    private AdapterFactory themeAdaptorFactory;

    @Override
    public AssetLibrary library(Resource resource) {
        return libraryAdaptorFactory.getAdapter(resource, AssetLibrary.class);
    }

    @Override
    public AssetTheme theme(Resource resource) {
        return themeAdaptorFactory.getAdapter(resource, AssetTheme.class);
    }

}
