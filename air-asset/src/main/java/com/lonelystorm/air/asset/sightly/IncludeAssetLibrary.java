package com.lonelystorm.air.asset.sightly;

import static java.lang.String.format;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Set;

import com.adobe.cq.sightly.WCMUse;
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.models.AssetThemeConfiguration;
import com.lonelystorm.air.asset.services.LibraryResolver;
import com.lonelystorm.air.asset.util.AssetLibraryUtil;

public class IncludeAssetLibrary extends WCMUse {

    private Set<AssetLibrary> libraries;

    private Set<AssetTheme> subLibraries;

    private AssetThemeConfiguration themeConfig;

    private String extraAttributes;

    @Override
    public void activate() throws Exception {
        LibraryResolver resolver = getSlingScriptHelper().getService(LibraryResolver.class);
        String categories = get("categories", String.class);
        String themes = get("themes", String.class);
        String themeConfigPath = get("themeConfigPath", String.class);

        extraAttributes = get("extraAttributes", String.class);
        libraries = AssetLibraryUtil.categories(resolver, categories);
        themeConfig = AssetLibraryUtil.findThemeConfigurationByPath(resolver, themeConfigPath);
        subLibraries = AssetLibraryUtil.themes(resolver, libraries, themes);
    }

    public String include() {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);

        for (AssetLibrary library : libraries) {
            writer.println(AssetLibraryUtil.include(library.getPath(), "css", extraAttributes));
        }

        if (themeConfig == null) {
            for (AssetTheme theme : subLibraries) {
                writer.println(AssetLibraryUtil.include(theme.getPath(), "css", extraAttributes));
            }
        } else {
            for (AssetTheme themeLib : subLibraries) {
                if (themeLib.equals(themeConfig.getBaseTheme())) {
                    writer.println(AssetLibraryUtil.include(format("%s.%s%s", themeLib.getPath(), themeConfig.getUniqueName(), cacheBusterSelector(themeConfig.getLastModified())), "css", extraAttributes));
                } else {
                    writer.println(AssetLibraryUtil.include(themeLib.getPath(), "css", extraAttributes));
                }
            }
        }

        return sw.toString();
    }

    private String cacheBusterSelector(Calendar calendar) {
        if (calendar == null) {
            return "";
        }
        return format(".v-%d-v", calendar.getTime().getTime());
    }

}
