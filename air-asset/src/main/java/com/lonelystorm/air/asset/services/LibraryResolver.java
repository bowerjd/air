package com.lonelystorm.air.asset.services;

import java.util.Collection;
import java.util.List;

import com.lonelystorm.air.asset.models.Asset;
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;

public interface LibraryResolver {

    Asset load(String path);

    void clear();

    void add(AssetLibrary library);

    Collection<AssetLibrary> findAllLibraries();

    AssetLibrary findLibraryBySource(String source);

    AssetLibrary findLibraryByPath(String path);

    List<AssetLibrary> findLibrariesByCategory(String category);

    Collection<AssetTheme> findAllThemes();

    AssetTheme findThemeByPath(String path);

    List<AssetTheme> findThemesByTheme(String theme);

}
