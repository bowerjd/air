package com.lonelystorm.aem.air.asset.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.impl.MockAdapter;
import org.apache.sling.testing.resourceresolver.MockHelper;
import org.apache.sling.testing.resourceresolver.MockResourceResolverFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.lonelystorm.aem.air.asset.models.AssetLibrary;
import com.lonelystorm.aem.air.asset.models.AssetTheme;
import com.lonelystorm.aem.air.asset.services.LibraryResolver;

@RunWith(MockitoJUnitRunner.class)
public class AssetLibraryUtilTest {

    @Mock
    private LibraryResolver libraryResolver;

    @Mock
    private Resource resource;

    @Test
    public void categories() {
        when(resource.getPath()).thenReturn("/test/library");
        AssetLibrary library = MockAdapter.create(resource, AssetLibrary.class);

        List<AssetLibrary> libraries = new ArrayList<>();
        libraries.add(library);
        when(libraryResolver.findLibrariesByCategory("testing")).thenReturn(libraries);

        Set<AssetLibrary> libs = AssetLibraryUtil.categories(libraryResolver, "testing");
        assertTrue(libs.contains(library));
        assertEquals(1, libs.size());
    }

    @Test
    public void themes() throws Exception {
        ResourceResolverFactory factory = new MockResourceResolverFactory();
        ResourceResolver resolver = factory.getResourceResolver(null);

        MockHelper.create(resolver)
            .resource("/library")
                .p("categories", new String[] { "testing" })
                .resource("theme")
                .p(JcrConstants.JCR_PRIMARYTYPE, LibraryConstants.ASSET_THEME_TYPE_NAME)
        .add();

        AssetLibrary library = MockAdapter.create(resolver.getResource("/library"), AssetLibrary.class);
        Set<AssetLibrary> libraries = new HashSet<>();
        libraries.add(library);

        AssetTheme theme = MockAdapter.create(resource, AssetTheme.class);

        Set<AssetTheme> themes = AssetLibraryUtil.themes(libraryResolver, libraries, "testing");
        assertTrue(themes.contains(theme));
        assertEquals(1, themes.size());
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
