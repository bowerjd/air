package com.lonelystorm.air.asset.services;

import com.lonelystorm.air.asset.models.Asset;

public interface CacheManager {

    void cache(String path, String compiled);

    String get(String path);

    void clear();

    void clear(Asset asset);

}
