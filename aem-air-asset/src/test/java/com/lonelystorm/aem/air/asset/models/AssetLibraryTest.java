package com.lonelystorm.aem.air.asset.models;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssetLibraryTest {

    private ResourceResolver resolver;

    @Before
    public void setUp() throws Exception {
        resolver = Repository.create();
    }

    @Test
    public void loadPaths() {
        Resource resource = resolver.getResource("/library");
        AssetLibrary library = resource.adaptTo(AssetLibrary.class);

        assertArrayEquals(new String[] { "/library/one", "/library/two" }, library.getLoadPaths());
    }


    @Test
    public void path() {
        Resource resource = resolver.getResource("/library");
        AssetLibrary library = resource.adaptTo(AssetLibrary.class);

        assertEquals("/library", library.getPath());
    }

    @Test
    public void sources() {
        Resource resource = resolver.getResource("/library");
        AssetLibrary library = resource.adaptTo(AssetLibrary.class);

        Set<String> sources = library.getSources();
        assertTrue(sources.contains("/library/module.scss"));
        assertEquals(1, sources.size());
    }

    @Test
    public void categories() {
        Resource resource = resolver.getResource("/library");
        AssetLibrary library = resource.adaptTo(AssetLibrary.class);

        assertArrayEquals(new String[] { "team" }, library.getCategories());
    }

    @Test
    public void embed() {
        Resource resource = resolver.getResource("/library");
        AssetLibrary library = resource.adaptTo(AssetLibrary.class);

        assertArrayEquals(new String[] { "pvp" }, library.getEmbed());
    }

    @Test
    public void themes() {
        Resource resource = resolver.getResource("/library");
        AssetLibrary library = resource.adaptTo(AssetLibrary.class);

        AssetTheme theme1 = resolver.getResource("/library/theme1").adaptTo(AssetTheme.class);
        AssetTheme theme2 = resolver.getResource("/library/theme1").adaptTo(AssetTheme.class);

        Set<AssetTheme> themes = library.getThemes();
        assertTrue(themes.contains(theme1));
        assertTrue(themes.contains(theme2));
        assertEquals(2, themes.size());
    }

    @Test
    public void hash() {
        Resource resource = resolver.getResource("/library");
        AssetLibrary library = resource.adaptTo(AssetLibrary.class);

        assertEquals(-1371711113, library.hashCode());
        assertNotEquals(library.hashCode(), resolver.getResource("/library2").adaptTo(AssetLibrary.class).hashCode());
    }

    @Test
    public void equals() {
        Resource resource = resolver.getResource("/library");
        AssetLibrary library = resource.adaptTo(AssetLibrary.class);

        assertFalse(library.equals(null));
        assertTrue(library.equals(library));
        assertFalse(library.equals("Testing"));
        assertTrue(library.equals(resolver.getResource("/library").adaptTo(AssetLibrary.class)));
        assertFalse(library.equals(resolver.getResource("/library2").adaptTo(AssetLibrary.class)));
    }

}
