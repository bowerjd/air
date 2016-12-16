package com.lonelystorm.air.asset.tags;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.lonelystorm.air.AemContextTest;
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.models.Repository;
import com.lonelystorm.air.asset.services.LibraryResolver;

@RunWith(MockitoJUnitRunner.class)
public class IncludeAssetLibraryTest extends AemContextTest {

    @Mock
    private JspWriter jspWriter;

    @Mock
    private LibraryResolver libraryResolver;

    @Mock
    private PageContext pageContext;

    private IncludeAssetLibrary includeAssetLibrary = new IncludeAssetLibrary();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Repository.create(resolver);

        // OSGi Services
        context.registerService(LibraryResolver.class, libraryResolver);

        when(pageContext.getRequest()).thenReturn(context.request());
        when(pageContext.getOut()).thenReturn(jspWriter);
    }

    @Test
    public void categories() throws Exception {
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
