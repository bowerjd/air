package com.lonelystorm.air.asset.sightly;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.lonelystorm.air.asset.models.AssetThemeConfiguration;
import com.lonelystorm.air.asset.models.Repository;
import com.lonelystorm.air.asset.services.LibraryResolver;

@RunWith(MockitoJUnitRunner.class)
public class IncludeAssetLibraryTest extends AemContextTest {

    @Mock
    private LibraryResolver libraryResolver;

    private IncludeAssetLibrary includeAssetLibrary = new IncludeAssetLibrary();

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
        bindings.put("categories", "categories");


        List<AssetLibrary> libraries = new ArrayList<>();
        libraries.add(resolver.getResource("/library").adaptTo(AssetLibrary.class));
        when(libraryResolver.findLibrariesByCategory("categories")).thenReturn(libraries);

        includeAssetLibrary.init(bindings);
        String include = includeAssetLibrary.include();

        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/library.css\">");
        assertEquals(sw.toString(), include);
    }

    @Test
    public void themes() throws Exception {
        bindings.put("categories", "categories");
        bindings.put("themes", "blue");

        List<AssetLibrary> libraries = new ArrayList<>();
        libraries.add(resolver.getResource("/library").adaptTo(AssetLibrary.class));
        when(libraryResolver.findLibrariesByCategory("categories")).thenReturn(libraries);

        includeAssetLibrary.init(bindings);
        String include = includeAssetLibrary.include();

        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/library.css\">");
        writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/library/theme1.css\">");
        assertEquals(sw.toString(), include);
    }

}
