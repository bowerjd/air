package com.lonelystorm.air.asset.services.impl;

import static com.lonelystorm.air.asset.util.PropertiesUtil.comparePropertyValue;
import static com.lonelystorm.air.asset.util.PropertiesUtil.containsProperty;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import com.lonelystorm.air.asset.services.FileResolver;
import com.lonelystorm.air.util.EscalatedResolver;

@Component
@Service
public class FileResolverImpl implements FileResolver {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public boolean exists(final String location) {
        EscalatedResolver escalated = new EscalatedResolver(resourceResolverFactory, getClass());
        return escalated.doSession(new EscalatedResolver.Session<Boolean>() {

            @Override
            public Boolean run(ResourceResolver resolver) {
                Resource resource = resolver.getResource(location);
                if (resource != null && resource.getChild("jcr:content") != null) {
                    resource = resource.getChild("jcr:content");
                }
                ValueMap properties = ResourceUtil.getValueMap(resource);

                if (comparePropertyValue(properties, "jcr:primaryType", "nt:resource") && containsProperty(properties, "jcr:data")) {
                    return true;
                }

                return false;
            }

        });
    }

    @Override
    public String load(final String location) {
        EscalatedResolver escalated = new EscalatedResolver(resourceResolverFactory, getClass());
        return escalated.doSession(new EscalatedResolver.Session<String>() {

            @Override
            public String run(ResourceResolver resolver) {
                Resource resource = resolver.getResource(location);
                if (resource != null && resource.getChild("jcr:content") != null) {
                    resource = resource.getChild("jcr:content");
                }
                ValueMap properties = ResourceUtil.getValueMap(resource);

                if (comparePropertyValue(properties, "jcr:primaryType", "nt:resource") && containsProperty(properties, "jcr:data")) {
                    InputStream is = properties.get("jcr:data", InputStream.class);
                    String source = null;

                    try {
                        source = IOUtils.toString(is);
                    } catch (IOException e) {
                        // TODO: Log me
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
