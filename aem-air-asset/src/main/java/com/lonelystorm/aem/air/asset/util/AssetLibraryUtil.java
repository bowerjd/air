package com.lonelystorm.aem.air.asset.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lonelystorm.aem.air.asset.models.AssetLibrary;
import com.lonelystorm.aem.air.asset.models.AssetTheme;
import com.lonelystorm.aem.air.asset.services.LibraryResolver;

public class AssetLibraryUtil {

    public static Set<AssetLibrary> categories(LibraryResolver resolver, String categories) {
        Set<AssetLibrary> libraries = new HashSet<>();

        for (String category : split(categories)) {
            List<AssetLibrary> library = resolver.findLibrariesByCategory(category);
            if (library != null) {
                libraries.addAll(library);
            }
        }

        return libraries;
    }

    public static Set<AssetTheme> themes(LibraryResolver resolver, Set<AssetLibrary> categories, String themes) {
        List<String> sThemes = split(themes);
        Set<AssetTheme> libraries = new HashSet<>();

        for (AssetLibrary category : categories) {
            Set<AssetTheme> categoryThemes = category.getThemes();
            for (AssetTheme categoryTheme : categoryThemes) {
                for (String theme : sThemes) {
                    if (Arrays.asList(categoryTheme.getThemes()).contains(theme)) {
                        libraries.add(categoryTheme);
                    }
                }
            }
        }

        return libraries;
    }

    public static List<String> split(String text) {
        List<String> parts = new ArrayList<>();

        String[] split = text.split(",");
        if (split != null) {
            for (String part : split) {
                parts.add(part.trim());
            }
        }

        return parts;
    }

    public static String include(String path, String extension) {
        return String.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s.%s\">", path, extension);
    }

}
