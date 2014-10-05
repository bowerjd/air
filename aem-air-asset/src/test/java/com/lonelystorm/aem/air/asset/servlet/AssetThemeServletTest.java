package com.lonelystorm.aem.air.asset.servlet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.lonelystorm.aem.air.asset.models.AssetTheme;
import com.lonelystorm.aem.air.asset.models.Repository;
import com.lonelystorm.aem.air.asset.services.CompilerManager;
import com.lonelystorm.aem.air.asset.services.LibraryResolver;

@RunWith(MockitoJUnitRunner.class)
public class AssetThemeServletTest {

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private SlingHttpServletResponse response;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private RequestPathInfo requestPathInfo;

    @Mock
    private CompilerManager compilerManager;

    @Mock
    private LibraryResolver libraryResolver;

    @InjectMocks
    private AssetThemeServlet assetThemeServlet;

    @Test
    public void doGet() throws Exception {
        ResourceResolver resolver = Repository.create();
        AssetTheme theme = resolver.getResource("/library/theme1").adaptTo(AssetTheme.class);

        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(requestPathInfo.getResourcePath()).thenReturn("/library/theme1");
        when(response.getWriter()).thenReturn(printWriter);

        when(libraryResolver.findThemeByPath("/library/theme1")).thenReturn(theme);
        when(compilerManager.compile(theme)).thenReturn("SUCCESSFUL TEST");

        assetThemeServlet.doGet(request, response);

        verify(response, times(1)).setCharacterEncoding("UTF-8");
        verify(printWriter, times(1)).append("SUCCESSFUL TEST");
    }

    @Test(expected = IOException.class)
    public void doGetIOException() throws Exception {
        ResourceResolver resolver = Repository.create();
        AssetTheme theme = resolver.getResource("/library/theme1").adaptTo(AssetTheme.class);

        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(requestPathInfo.getResourcePath()).thenReturn("/library/theme1");
        when(response.getWriter()).thenThrow(new IOException());

        when(libraryResolver.findThemeByPath("/library/theme1")).thenReturn(theme);
        when(compilerManager.compile(theme)).thenReturn("SUCCESSFUL TEST");

        assetThemeServlet.doGet(request, response);
    }

    @Test
    public void doGetNonExistantTheme() throws Exception {
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(requestPathInfo.getResourcePath()).thenReturn("/library/theme1");

        when(libraryResolver.findLibraryByPath("/library/theme1")).thenReturn(null);

        assetThemeServlet.doGet(request, response);

        verify(response, times(1)).sendError(eq(404), any(String.class));
    }

}
