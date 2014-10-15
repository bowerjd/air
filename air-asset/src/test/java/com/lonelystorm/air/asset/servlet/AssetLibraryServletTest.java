package com.lonelystorm.air.asset.servlet;

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

import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.Repository;
import com.lonelystorm.air.asset.services.CompilerManager;
import com.lonelystorm.air.asset.services.LibraryResolver;
import com.lonelystorm.air.asset.servlet.AssetLibraryServlet;

@RunWith(MockitoJUnitRunner.class)
public class AssetLibraryServletTest {

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
    private AssetLibraryServlet assetLibraryServlet;

    @Test
    public void doGet() throws Exception {
        ResourceResolver resolver = Repository.create();
        AssetLibrary library = resolver.getResource("/library").adaptTo(AssetLibrary.class);

        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(requestPathInfo.getResourcePath()).thenReturn("/library");
        when(response.getWriter()).thenReturn(printWriter);

        when(libraryResolver.findLibraryByPath("/library")).thenReturn(library);
        when(compilerManager.compile(library)).thenReturn("SUCCESSFUL TEST");

        assetLibraryServlet.doGet(request, response);

        verify(response, times(1)).setCharacterEncoding("UTF-8");
        verify(printWriter, times(1)).append("SUCCESSFUL TEST");
    }

    @Test(expected = IOException.class)
    public void doGetIOException() throws Exception {
        ResourceResolver resolver = Repository.create();
        AssetLibrary library = resolver.getResource("/library").adaptTo(AssetLibrary.class);

        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(requestPathInfo.getResourcePath()).thenReturn("/library");
        when(response.getWriter()).thenThrow(new IOException());

        when(libraryResolver.findLibraryByPath("/library")).thenReturn(library);
        when(compilerManager.compile(library)).thenReturn("SUCCESSFUL TEST");

        assetLibraryServlet.doGet(request, response);
    }

    @Test
    public void doGetNonExistantLibrary() throws Exception {
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(requestPathInfo.getResourcePath()).thenReturn("/library");

        when(libraryResolver.findLibraryByPath("/library")).thenReturn(null);

        assetLibraryServlet.doGet(request, response);

        verify(response, times(1)).sendError(eq(404), any(String.class));
    }

}
