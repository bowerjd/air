package com.lonelystorm.air.asset.services;

import org.apache.sling.api.resource.Resource;

import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.models.AssetThemeConfiguration;

/**
 * The LibraryAdapterManager provides an interface to convert an resource into an asset.
 *
 * Due to a race condition present with immediate Components in Apache Felix,
 * this service must be used to adapt an resource.
 *
 * https://issues.apache.org/jira/browse/SLING-4026
 */
public interface LibraryAdapterManager {

    /**
     * Converts an resource into an AssetLibrary.
     *
     * @param resource
     *     The Resource to adapt
     * @return
     *     The AssetLibrary if the resource contained the required parameters and passed validation,
     *     otherwise null is returned.
     */
    public AssetLibrary library(Resource resource);

    /**
     * Converts an resource into an AssetTheme.
     *
     * @param resource
     *     The resource to adapt
     * @return
     *     The AssetTheme if the resource contaiend the required parameters and passed validation,
     *     otherwise null is returned.
     */
    public AssetTheme theme(Resource resource);

    /**
     * Converts a resource into an AssetThemeConfiguration
     * @param resource
     *     The resource to adapt
     * @return
     *     The AssetThemeConfiguration if the resource contaiend the required parameters and passed validation,
     *     otherwise null is returned.
     */
    public AssetThemeConfiguration themeConfiguration(Resource resource);

}
