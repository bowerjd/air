package com.lonelystorm.air.asset.servlet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.lonelystorm.air.AemContextTest;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.models.Repository;
import com.lonelystorm.air.asset.services.CompilerManager;
import com.lonelystorm.air.asset.services.LibraryResolver;

@RunWith(MockitoJUnitRunner.class)
public class AssetThemeServletTest extends AemContextTest {

    @Mock
    private PrintWriter printWriter;

    @Mock
    private CompilerManager compilerManager;

    @Mock
    private LibraryResolver libraryResolver;

    @InjectMocks
    private AssetThemeServlet assetThemeServlet;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Repository.create(resolver);

        // OSGi Services
        context.registerService(LibraryResolver.class, libraryResolver);
    }

    @Test
    public void doGet() throws Exception {
        AssetTheme theme = resolver.getResource("/library/theme1").adaptTo(AssetTheme.class);

        context.requestPathInfo().setResourcePath("/library/theme1");
        context.requestPathInfo().setExtension("css");

        when(libraryResolver.findThemeByPath("/library/theme1")).thenReturn(theme);
        when(compilerManager.compile(theme)).thenReturn("SUCCESSFUL TEST");

        assetThemeServlet.doGet(context.request(), context.response());

        assertEquals("UTF-8", context.response().getCharacterEncoding());
        assertEquals("text/css;charset=UTF-8", context.response().getContentType());
        assertEquals("SUCCESSFUL TEST", context.response().getOutputAsString());
    }

    @Test(expected = IOException.class)
    public void doGetIOException() throws Exception {
        AssetTheme theme = resolver.getResource("/library/theme1").adaptTo(AssetTheme.class);

        context.requestPathInfo().setResourcePath("/library/theme1");
        context.requestPathInfo().setExtension("css");

        final SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
        when(response.getWriter()).thenThrow(new IOException());

        when(libraryResolver.findThemeByPath("/library/theme1")).thenReturn(theme);
        when(compilerManager.compile(theme)).thenReturn("SUCCESSFUL TEST");

        assetThemeServlet.doGet(context.request(), response);
    }

    @Test
    public void doGetNonExistantTheme() throws Exception {
        context.requestPathInfo().setResourcePath("/library/theme1");
        context.requestPathInfo().setExtension("css");

        when(libraryResolver.findLibraryByPath("/library/theme1")).thenReturn(null);

        assetThemeServlet.doGet(context.request(), context.response());

        assertEquals(404, context.response().getStatus());
    }

}
