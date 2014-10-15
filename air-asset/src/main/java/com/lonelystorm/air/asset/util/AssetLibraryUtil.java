package com.lonelystorm.air.asset.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.services.LibraryResolver;

public class AssetLibraryUtil {

    private AssetLibraryUtil() {
    };

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

    public static Set<AssetTheme> themes(LibraryResolver resolver, Set<AssetLibrary> libraries, String categories) {
        List<String> splitCategories = split(categories);
        Set<AssetTheme> results = new HashSet<>();

        for (AssetLibrary library : libraries) {
            Set<AssetTheme> themes = library.getThemes();
            for (AssetTheme theme : themes) {
                for (String category : splitCategories) {
                    if (Arrays.asList(theme.getThemes()).contains(category)) {
                        results.add(theme);
                    }
                }
            }

        }

        return results;
    }

    public static List<String> split(String text) {
        List<String> parts = new ArrayList<>();

        if (text != null) {
            String[] split = text.split(",");
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
