package com.lonelystorm.air.asset.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.lonelystorm.air.asset.models.Asset;
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.models.AssetThemeConfiguration;

public interface LibraryResolver {

    Asset load(String path);

    AssetThemeConfiguration loadThemeConfiguration(String path);

    void clear();

    void add(AssetLibrary library);

    void addThemeConfiguration(AssetThemeConfiguration themeConfig);

    Collection<AssetLibrary> findAllLibraries();

    AssetLibrary findLibraryBySource(String source);

    AssetLibrary findLibraryByPath(String path);

    List<AssetLibrary> findLibrariesByCategory(String category);

    Collection<AssetTheme> findAllThemes();

    AssetTheme findThemeByPath(String path);

    Set<AssetTheme> findThemesByTheme(String theme);

    Collection<AssetThemeConfiguration> findAllThemeConfigurations();

    AssetThemeConfiguration findThemeConfigurationByPath(String path);

    AssetThemeConfiguration findThemeConfigurationByUniqueName(String selector, AssetTheme theme);
}
