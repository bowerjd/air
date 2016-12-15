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
import com.lonelystorm.air.asset.models.AssetThemeConfiguration;
import com.lonelystorm.air.asset.services.CompilerManager;
import com.lonelystorm.air.asset.services.LibraryResolver;

@SlingServlet(resourceTypes = { "ls/AssetThemeConfiguration" }, methods = "GET")
public class AssetThemeConfigurationServlet extends SlingSafeMethodsServlet {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetThemeConfigurationServlet.class);

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
        final AssetThemeConfiguration themeConf = libraryResolver.findThemeConfigurationByPath(resourcePath);

        if (themeConf == null) {
            response.sendError(404, String.format("Unable to render theme configuration/variant (%s)", resourcePath));
        } else {
            try {
                String code = compilerManager.compile(themeConf);

                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/css;charset=UTF-8");

                response.getWriter().append(code);
            } catch (CompilerException e) {
                response.sendError(500, String.format("Unable to render theme configuration/variant (%s)", resourcePath));
                LOGGER.error("Unable to render library", e);
            }
        }
    }

}
