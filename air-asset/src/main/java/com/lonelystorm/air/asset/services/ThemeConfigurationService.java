package com.lonelystorm.air.asset.services;

import com.lonelystorm.air.asset.models.AssetThemeConfiguration;

/**
 * OSGi Service interface to configure themes into unique theme variations.
 */
public interface ThemeConfigurationService {
    /**
     * @param config the theme configuration that will be used to augment the base-theme
     * @param themeSource the theme's source code that should be augmented
     * 
     * @return the processed (augmented) theme source that will be used with the <code>config</code> applied.
     */
    String augmentTheme(AssetThemeConfiguration config, String themeSource);

    /**
     * @param config the asset theme configuration
     * 
     * @return true if this service supports generating the <code>config</code>, false otherwise
     */
    boolean supports(AssetThemeConfiguration config);

}
