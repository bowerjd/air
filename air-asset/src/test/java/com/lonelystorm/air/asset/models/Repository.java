package com.lonelystorm.air.asset.models;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.resourceresolver.MockHelper;

import com.lonelystorm.air.asset.util.LibraryConstants;

public class Repository {

    /** Setup the repository with some sample content */
    public static ResourceResolver create(ResourceResolver resolver) throws Exception {

        MockHelper.create(resolver)
        .resource("/library")
            .p("categories", new String[] { "team" })
            .p("embed", new String[] { "pvp" })
            .p("precompile", true)
            .p("loadPaths", new String[] { "/library/one", "/library/two" })
                .resource("/library/theme1")
                    .p(JcrConstants.JCR_PRIMARYTYPE, LibraryConstants.ASSET_THEME_TYPE_NAME)
                    .p(ResourceResolver.PROPERTY_RESOURCE_TYPE, LibraryConstants.ASSET_THEME_TYPE_NAME)
                    .p("themes", new String[] { "blue" })
                    .p("loadPaths", new String[] { "/library/theme/one", "/library/theme/two" })
                        .resource("/library/theme1/theme.scss")
                        .p(JcrConstants.JCR_PRIMARYTYPE, "nt:file")
                        .p(ResourceResolver.PROPERTY_RESOURCE_TYPE, "nt:file")
                .resource("/library/theme2")
                    .p(JcrConstants.JCR_PRIMARYTYPE, LibraryConstants.ASSET_THEME_TYPE_NAME)
                    .p(ResourceResolver.PROPERTY_RESOURCE_TYPE, LibraryConstants.ASSET_THEME_TYPE_NAME)
                    .p("themes", new String[] { "red" })
                .resource("/library/theme3")
                    .p(JcrConstants.JCR_PRIMARYTYPE, LibraryConstants.ASSET_THEME_TYPE_NAME)
                    .p(ResourceResolver.PROPERTY_RESOURCE_TYPE, LibraryConstants.ASSET_THEME_TYPE_NAME)
                .resource("/library/module.scss")
                    .p(JcrConstants.JCR_PRIMARYTYPE, "nt:file")
                    .p(ResourceResolver.PROPERTY_RESOURCE_TYPE, "nt:file")
        .resource("/library2")
            .p("categories", new String[] { "team" })
            .p("embed", new String[] { "pvp" })
            .p("loadPaths", new String[] { "/library/one", "/library/two" })
        .resource("/etc").resource("/etc/themes").resource("foo-config").resource("jcr:content")
        .resource("/etc/themes/foo-config/jcr:content/config")
            .p(JcrConstants.JCR_PRIMARYTYPE, LibraryConstants.ASSET_THEME_CONFIG_NAME)
            .p("baseTheme", "blue")
            .p("uniqueName", "stylish-blue")
            .p("themeConfigType", "something")
        .commit();

        return resolver;
    }

}
