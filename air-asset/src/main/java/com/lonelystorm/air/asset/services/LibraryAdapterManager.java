package com.lonelystorm.air.asset.services;

import org.apache.sling.api.resource.Resource;

import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;

public interface LibraryAdapterManager {

    public AssetLibrary library(Resource resource);

    public AssetTheme theme(Resource resource);

}
