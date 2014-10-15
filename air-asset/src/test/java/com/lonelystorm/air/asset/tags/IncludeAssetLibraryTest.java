package com.lonelystorm.air.asset.tags;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.Repository;
import com.lonelystorm.air.asset.services.LibraryResolver;
import com.lonelystorm.air.asset.tags.IncludeAssetLibrary;

@RunWith(MockitoJUnitRunner.class)
public class IncludeAssetLibraryTest {

    @Mock
    private PageContext pageContext;

    @Mock
    private SlingHttpServletRequest slingHttpServletRequest;

    @Mock
    private SlingBindings slingBindings;

    @Mock
    private SlingScriptHelper slingScriptHelper;

    @Mock
    private JspWriter jspWriter;

    @Mock
    private LibraryResolver libraryResolver;

    private IncludeAssetLibrary includeAssetLibrary;

    @Before
    public void setUp() {
        includeAssetLibrary = spy(new IncludeAssetLibrary());

        when(pageContext.getRequest()).thenReturn(slingHttpServletRequest);
        when(slingHttpServletRequest.getAttribute(SlingBindings.class.getName())).thenReturn(slingBindings);
        when(slingBindings.getSling()).thenReturn(slingScriptHelper);
        when(slingScriptHelper.getService(LibraryResolver.class)).thenReturn(libraryResolver);
        when(pageContext.getOut()).thenReturn(jspWriter);
    }

    @Test
    public void categories() throws Exception {
        ResourceResolver resolver = Repository.create();
        List<AssetLibrary> libraries = new ArrayList<>();
        libraries.add(resolver.getResource("/library").adaptTo(AssetLibrary.class));
        when(libraryResolver.findLibrariesByCategory("categories")).thenReturn(libraries);

        includeAssetLibrary.setCategories("categories");
        includeAssetLibrary.setPageContext(pageContext);
        includeAssetLibrary.doStartTag();

        verify(jspWriter, times(1)).println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/library.css\">");
    }

    @Test
    public void themes() throws Exception {
        ResourceResolver resolver = Repository.create();
        List<AssetLibrary> libraries = new ArrayList<>();
        libraries.add(resolver.getResource("/library").adaptTo(AssetLibrary.class));
        when(libraryResolver.findLibrariesByCategory("categories")).thenReturn(libraries);

        includeAssetLibrary.setCategories("categories");
        includeAssetLibrary.setThemes("blue");
        includeAssetLibrary.setPageContext(pageContext);
        includeAssetLibrary.doStartTag();

        verify(jspWriter, times(1)).println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/library.css\">");
        verify(jspWriter, times(1)).println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/library/theme1.css\">");
    }

}
