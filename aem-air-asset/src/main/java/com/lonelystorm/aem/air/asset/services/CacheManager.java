package com.lonelystorm.aem.air.asset.services;

import com.lonelystorm.aem.air.asset.models.Asset;

public interface CacheManager {

    void cache(String path, String compiled);

    String get(String path);

    void clear();

    void clear(Asset asset);

}
