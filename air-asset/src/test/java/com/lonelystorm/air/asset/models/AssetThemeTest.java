package com.lonelystorm.air.asset.models;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.lonelystorm.air.AemContextTest;

@RunWith(MockitoJUnitRunner.class)
public class AssetThemeTest extends AemContextTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Repository.create(resolver);
    }

    @Test
    public void loadPaths() {
        Resource resource = resolver.getResource("/library/theme1");
        AssetTheme theme = resource.adaptTo(AssetTheme.class);

        assertArrayEquals(new String[] { "/library/theme/one", "/library/theme/two" }, theme.getLoadPaths());
    }

    @Test
    public void path() {
        Resource resource = resolver.getResource("/library/theme1");
        AssetTheme theme = resource.adaptTo(AssetTheme.class);

        assertEquals("/library/theme1", theme.getPath());
    }

    @Test
    public void sources() {
        Resource resource = resolver.getResource("/library/theme1");
        AssetTheme theme = resource.adaptTo(AssetTheme.class);

        Set<String> sources = theme.getSources();
        assertTrue(sources.contains("/library/theme1/theme.scss"));
        assertEquals(1, sources.size());
    }

    @Test
    public void categories() {
        Resource resource = resolver.getResource("/library/theme1");
        AssetTheme theme = resource.adaptTo(AssetTheme.class);

        assertArrayEquals(new String[] { "blue" }, theme.getThemes());
    }

    @Test
    public void hash() {
        Resource resource = resolver.getResource("/library/theme1");
        AssetTheme theme = resource.adaptTo(AssetTheme.class);

        assertEquals(239847786, theme.hashCode());
        assertNotEquals(theme.hashCode(), resolver.getResource("/library/theme2").adaptTo(AssetTheme.class).hashCode());
    }

    @Test
    public void equals() {
        Resource resource = resolver.getResource("/library/theme1");
        AssetTheme theme = resource.adaptTo(AssetTheme.class);

        assertFalse(theme.equals(null));
        assertTrue(theme.equals(theme));
        assertFalse(theme.equals("Testing"));
        assertTrue(theme.equals(resolver.getResource("/library/theme1").adaptTo(AssetTheme.class)));
        assertFalse(theme.equals(resolver.getResource("/library/theme2").adaptTo(AssetTheme.class)));
    }

}
