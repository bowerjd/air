package com.lonelystorm.air.component.services.impl;

import static org.apache.commons.lang.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.containsNone;
import static org.apache.commons.lang.StringUtils.startsWith;

import java.util.Dictionary;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import com.day.cq.rewriter.linkchecker.Link;
import com.day.cq.rewriter.linkchecker.LinkCheckerSettings;
import com.day.cq.rewriter.pipeline.RequestRewriter;

@Component(
    label = "LonelyStorm Air - Component - Request Rewriter HTML Extension",
    description = "Rewrites links that references a resource directly to append the .html extension.",
    policy = ConfigurationPolicy.REQUIRE,
    metatype = true
)
@Service
@Properties({
    @Property(
        name = "requestrewriter.paths",
        label = "Paths",
        description = "Only rewrites paths to add the .html extension if they start with one of the above paths.",
        unbounded = PropertyUnbounded.ARRAY
    )
})
public class RequestRewriterImpl implements RequestRewriter {

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestRewriterImpl.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private String[] paths;

    /**
     * Activate.
     *
     * @param componentContext
     */
    @Activate
    protected void activate(final ComponentContext componentContext) {
        final Dictionary<?, ?> properties = componentContext.getProperties();

        paths = PropertiesUtil.toStringArray(properties.get("requestrewriter.paths"), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attributes rewrite(String elementName, Attributes attributes, LinkCheckerSettings settings) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String rewriteLink(Link link, LinkCheckerSettings settings) {
        final String href = link.getHref();

        if (isNotEmpty(paths) && containsNone(href, ".")) {
            ResourceResolver resolver = null;
            try {
                resolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

                for (String path : paths) {
                    if (startsWith(href, path) && resolver.getResource(href) != null) {
                        return String.format("%s.html", resolver.map(href));
                    }
                }
            } catch (LoginException e) {
                LOGGER.error("Unable to create resourceresolver to rewrite links", e);
            } finally {
                closeQuietly(resolver);
            }
        }

        return null;
    }

    private void closeQuietly(ResourceResolver resolver) {
        if (resolver != null) {
            resolver.close();
        }
    }

}
