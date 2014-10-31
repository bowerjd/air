package com.lonelystorm.air.asset.services;


public interface CacheManager {

    void cache(String path, String compiled);

    String get(String path);

    void clear();

}
