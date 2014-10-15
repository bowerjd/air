package com.lonelystorm.air.asset.sightly;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

import com.adobe.cq.sightly.WCMUse;
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.services.LibraryResolver;
import com.lonelystorm.air.asset.util.AssetLibraryUtil;

public class IncludeAssetLibrary extends WCMUse {

    private Set<AssetLibrary> libraries;

    private Set<AssetTheme> subLibraries;

    @Override
    public void activate() throws Exception {
        LibraryResolver resolver = getSlingScriptHelper().getService(LibraryResolver.class);
        String categories = get("categories", String.class);
        String themes = get("themes", String.class);

        libraries = AssetLibraryUtil.categories(resolver, categories);
        subLibraries = AssetLibraryUtil.themes(resolver, libraries, themes);
    }

    public String include() {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);

        for (AssetLibrary library : libraries) {
            writer.println(AssetLibraryUtil.include(library.getPath(), "css"));
        }

        for (AssetTheme theme : subLibraries) {
            writer.println(AssetLibraryUtil.include(theme.getPath(), "css"));
        }

        return sw.toString();
    }

}
