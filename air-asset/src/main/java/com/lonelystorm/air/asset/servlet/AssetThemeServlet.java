package com.lonelystorm.air.asset.servlet;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lonelystorm.air.asset.exceptions.CompilerException;
import com.lonelystorm.air.asset.models.AssetTheme;
import com.lonelystorm.air.asset.models.AssetThemeConfiguration;
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
        final AssetTheme theme = libraryResolver.findThemeByPath(resourcePath);

        if (theme == null) {
            response.sendError(404, String.format("Unable to render library (%s)", resourcePath));
            return;
        }

        final String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors.length <= 0) {
            try {
                writeResponse(response, compilerManager.compile(theme));
            } catch (CompilerException e) {
                response.sendError(500, String.format("Unable to render library (%s)", resourcePath));
                LOGGER.error("Unable to render library", e);
            }
        } else {
            AssetThemeConfiguration themeConf = locateThemeConfiguration(selectors, theme);
            if (themeConf == null) {
                response.sendError(404, String.format("Unable to locate theme configuration (%s, %s)", resourcePath, StringUtils.join(selectors, ".")));
            } else {
                try {
                    writeResponse(response, compilerManager.compile(themeConf));
                } catch (CompilerException e) {
                    response.sendError(500, String.format("Unable to render %s from (%s.%s)", themeConf, resourcePath, StringUtils.join(selectors, ".")));
                    LOGGER.error("Unable to render configured-theme {} from ({},{})", themeConf, resourcePath, StringUtils.join(selectors, "."), e);
                    return;
                }
            }
        }

    }

    private AssetThemeConfiguration locateThemeConfiguration(String[] selectors, final AssetTheme theme) {
        for (String selector : selectors) {
            final AssetThemeConfiguration conf = libraryResolver.findThemeConfigurationByUniqueName(selector, theme);
            if (conf != null) {
                return conf;
            }
        }
        return null;
    }

    private void writeResponse(SlingHttpServletResponse response, String code) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/css;charset=UTF-8");

        response.getWriter().append(code);
    }

}
