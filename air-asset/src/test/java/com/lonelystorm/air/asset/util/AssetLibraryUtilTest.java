package com.lonelystorm.air.asset.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.lonelystorm.air.AemContextTest;
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.models.Repository;
import com.lonelystorm.air.asset.services.LibraryResolver;
import com.lonelystorm.air.asset.util.AssetLibraryUtil;

@RunWith(MockitoJUnitRunner.class)
public class AssetLibraryUtilTest extends AemContextTest {

    @Mock
    private LibraryResolver libraryResolver;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Repository.create(resolver);

        // OSGi Services
        context.registerService(LibraryResolver.class, libraryResolver);
    }

    @Test
    public void categories() throws Exception {

        AssetLibrary library = resolver.getResource("/library").adaptTo(AssetLibrary.class);
        List<AssetLibrary> libraries = new ArrayList<>();
        libraries.add(library);
        when(libraryResolver.findLibrariesByCategory("testing")).thenReturn(libraries);

        Set<AssetLibrary> libs = AssetLibraryUtil.categories(libraryResolver, "testing");
        assertTrue(libs.contains(library));
        assertEquals(1, libs.size());
    }

    @Test
    public void categoriesNoneExistant() throws Exception {
        List<AssetLibrary> libraries = new ArrayList<>();
        when(libraryResolver.findLibrariesByCategory("nonExistant")).thenReturn(libraries);

        Set<AssetLibrary> libs = AssetLibraryUtil.categories(libraryResolver, "nonExistant");
        assertEquals(0, libs.size());
    }

    @Test
    public void categoriesEmpty() throws Exception {
        when(libraryResolver.findLibrariesByCategory("nonExistant")).thenReturn(null);

        Set<AssetLibrary> libs = AssetLibraryUtil.categories(libraryResolver, "nonExistant");
        assertEquals(0, libs.size());
    }

    @Test
    public void themes() throws Exception {
        AssetLibrary library = resolver.getResource("/library").adaptTo(AssetLibrary.class);
        Set<AssetLibrary> libraries = new HashSet<>();
        libraries.add(library);

        AssetTheme theme = resolver.getResource("/library/theme1").adaptTo(AssetTheme.class);
        theme.equals(theme);

        Set<AssetTheme> themes = AssetLibraryUtil.themes(libraryResolver, libraries, "blue");
        assertTrue(themes.contains(theme));
        assertEquals(1, themes.size());
    }

    @Test
    public void themesNoneExistant() throws Exception {
        AssetLibrary library = resolver.getResource("/library").adaptTo(AssetLibrary.class);
        Set<AssetLibrary> libraries = new HashSet<>();
        libraries.add(library);

        AssetTheme theme = resolver.getResource("/library/theme1").adaptTo(AssetTheme.class);
        theme.equals(theme);

        Set<AssetTheme> themes = AssetLibraryUtil.themes(libraryResolver, libraries, "nonExistant");
        assertEquals(0, themes.size());
    }

    @Test
    public void split() {
        // One Item.
        List<String> split = AssetLibraryUtil.split("testing");
        assertEquals("testing", split.get(0));
        assertEquals(1, split.size());

        // Two Items.
        split = AssetLibraryUtil.split("testing,split");
        assertEquals("testing", split.get(0));
        assertEquals("split", split.get(1));
        assertEquals(2, split.size());

        // Three Items Trimmed.
        split = AssetLibraryUtil.split("testing,split , space");
        assertEquals("testing", split.get(0));
        assertEquals("split", split.get(1));
        assertEquals("space", split.get(2));
        assertEquals(3, split.size());

        // Empty.
        split = AssetLibraryUtil.split(null);
        assertEquals(0, split.size());
    }

    @Test
    public void include() {
        assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"module.css\">", AssetLibraryUtil.include("module", "css"));
        assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"test/module.css\">", AssetLibraryUtil.include("test/module", "css"));
        assertEquals("<link rel=\"stylesheet\" type=\"text/css\" href=\"/root/test/module.css\">", AssetLibraryUtil.include("/root/test/module", "css"));
    }

}
