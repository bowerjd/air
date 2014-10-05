package com.lonelystorm.aem.air.asset.servlet;

import java.io.IOException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import com.lonelystorm.aem.air.asset.models.Asset;
import com.lonelystorm.aem.air.asset.services.CompilerManager;
import com.lonelystorm.aem.air.asset.services.LibraryResolver;

@SlingServlet(resourceTypes = { "ls/AssetTheme" }, methods = "GET")
public class AssetThemeServlet extends SlingSafeMethodsServlet {

    /**
     * Default Serial Version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Asset Library Compiler
     */
    @Reference
    private transient CompilerManager compilerManager;

    /**
     * Asset Library Resolver
     */
    @Reference
    private transient LibraryResolver libraryResolver;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        final String resourcePath = request.getRequestPathInfo().getResourcePath();
        final Asset library = libraryResolver.findThemeByPath(resourcePath);

        if (library == null) {
            response.sendError(404, String.format("Unable to render library (%s)", resourcePath));
        } else {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/css;charset=UTF-8");

            String code = compilerManager.compile(library);
            response.getWriter().append(code);
        }
    }

}
