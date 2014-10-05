package com.lonelystorm.aem.air.asset.tags;

import java.io.IOException;
import java.util.Set;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.scripting.jsp.util.TagUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tldgen.BodyContentType;
import tldgen.Tag;
import tldgen.TagAttribute;

import com.lonelystorm.aem.air.asset.models.AssetLibrary;
import com.lonelystorm.aem.air.asset.models.AssetTheme;
import com.lonelystorm.aem.air.asset.services.LibraryResolver;
import com.lonelystorm.aem.air.asset.util.AssetLibraryUtil;

@Tag(bodyContentType = BodyContentType.EMPTY)
public class IncludeAssetLibrary extends TagSupport {

    /**
     * Default Serial Version
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(IncludeAssetLibrary.class);

    private String categories;

    private String themes;

    @TagAttribute(required = true, runtimeValueAllowed = true)
    public void setCategories(String categories) {
        this.categories = categories;
    }

    @TagAttribute(runtimeValueAllowed = true)
    public void setThemes(String themes) {
        this.themes = themes;
    }

    @Override
    public int doStartTag() {
        final SlingHttpServletRequest request = TagUtil.getRequest(pageContext);
        final SlingBindings bindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
        final SlingScriptHelper helper = bindings.getSling();

        final LibraryResolver resolver = helper.getService(LibraryResolver.class);
        final Set<AssetLibrary> libraries = AssetLibraryUtil.categories(resolver, categories);
        final Set<AssetTheme> subLibraries = AssetLibraryUtil.themes(resolver, libraries, themes);

        try {
            final JspWriter writer = pageContext.getOut();

            for (AssetLibrary library : libraries) {
                writer.println(AssetLibraryUtil.include(library.getPath(), "css"));
            }

            for (AssetTheme theme : subLibraries) {
                writer.println(AssetLibraryUtil.include(theme.getPath(), "css"));
            }
        } catch (IOException e) {
            LOGGER.error("Unable to generate asset library html markup", e);
        }

        return TagSupport.SKIP_BODY;
    }

}
