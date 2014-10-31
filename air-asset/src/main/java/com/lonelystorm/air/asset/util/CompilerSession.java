package com.lonelystorm.air.asset.util;

import static com.lonelystorm.air.asset.util.PropertiesUtil.comparePropertyValue;
import static com.lonelystorm.air.asset.util.PropertiesUtil.containsProperty;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lonelystorm.air.util.EscalatedResolver;

public class CompilerSession {

    final EscalatedResolver escalated;

    final Logger logger;

    public CompilerSession(ResourceResolverFactory factory, Class<?> callee) {
        escalated = new EscalatedResolver(factory, callee);
        logger = LoggerFactory.getLogger(callee);
    }

    public String file(final String path) {
        return escalated.doSession(new EscalatedResolver.Session<String>() {

            @Override
            public String run(ResourceResolver resolver) {
                final Resource resource = resolver.getResource(String.format("%s/jcr:content", path));
                final ValueMap properties = ResourceUtil.getValueMap(resource);

                if (comparePropertyValue(properties, "jcr:primaryType", "nt:resource") && containsProperty(properties, "jcr:data")) {
                    final InputStream is = properties.get("jcr:data", InputStream.class);
                    String source = null;

                    try {
                        source = IOUtils.toString(is);
                    } catch (IOException e) {
                        logger.error("Unable to read file contents", e);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }

                    return source;
                }

                return null;
            }

        });
    }

}
