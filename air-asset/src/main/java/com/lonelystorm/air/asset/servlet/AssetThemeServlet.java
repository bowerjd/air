package com.lonelystorm.air.asset.servlet;

import java.io.IOException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lonelystorm.air.asset.exceptions.CompilerException;
import com.lonelystorm.air.asset.models.Asset;
import com.lonelystorm.air.asset.services.CompilerManager;
import com.lonelystorm.air.asset.services.LibraryResolver;

@SlingServlet(resourceTypes = { "ls/AssetTheme" }, methods = "GET")
public class AssetThemeServlet extends SlingSafeMethodsServlet {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetThemeServlet.class);

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
            try {
                String code = compilerManager.compile(library);

                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/css;charset=UTF-8");

                response.getWriter().append(code);
            } catch (CompilerException e) {
                response.sendError(500, String.format("Unable to render library (%s)", resourcePath));
                LOGGER.error("Unable to render library", e);
            }
        }
    }

}
