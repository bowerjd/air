package com.lonelystorm.air.asset.services.impl;

import static com.lonelystorm.air.asset.util.PropertiesUtil.comparePropertyValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.ComponentContext;

import com.lonelystorm.air.asset.models.Asset;
import com.lonelystorm.air.asset.services.CacheManager;
import com.lonelystorm.air.asset.util.PropertiesUtil;
import com.lonelystorm.air.util.EscalatedResolver;

@Component(immediate = true)
@Service
public class CacheManagerImpl implements CacheManager {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private static final String DIRECTORY = "/var/lonelystorm/air/asset";

    private static final String FOLDER_TYPE = "sling:Folder";

    @Activate
    protected void activate(ComponentContext context) {
        EscalatedResolver escalated = new EscalatedResolver(resourceResolverFactory, getClass());
        escalated.doSession(new EscalatedResolver.Session<Boolean>() {

            @Override
            public Boolean run(ResourceResolver resolver) {
                try {
                    ResourceUtil.getOrCreateResource(resolver, DIRECTORY, FOLDER_TYPE, FOLDER_TYPE, true);
                } catch (PersistenceException e) {
                    // TODO Log me
                }

                return true;
            }

        });
    }

    @Override
    public void cache(final String path, final String compiled) {
        synchronized (this) {
            EscalatedResolver escalated = new EscalatedResolver(resourceResolverFactory, getClass());
            escalated.doSession(new EscalatedResolver.Session<Boolean>() {

                @Override
                public Boolean run(ResourceResolver resolver) {
                    String normalizedPath = FilenameUtils.normalize(String.format("%s/%s", DIRECTORY, path), true);
                    String folder = String.format("/%s", FilenameUtils.getPathNoEndSeparator(normalizedPath));
                    String name = FilenameUtils.getName(normalizedPath);

                    try {
                        Resource resource = ResourceUtil.getOrCreateResource(resolver, folder, FOLDER_TYPE, FOLDER_TYPE, true);
                        if (resource != null) {
                            Map<String, Object> properties = new TreeMap<String, Object>();
                            properties.put("jcr:primaryType", "nt:file");
                            Resource child = resolver.create(resource, name, properties);

                            properties = new TreeMap<String, Object>();
                            properties.put("jcr:primaryType", "nt:resource");
                            properties.put("jcr:data", IOUtils.toInputStream(compiled));
                            properties.put("jcr:encoding", "utf8");
                            resolver.create(child, "jcr:content", properties);
                        }

                        resolver.commit();
                    } catch (PersistenceException e) {
                        // TODO: Log me
                    }

                    return true;
                }

            });
        }
    }

    @Override
    public String get(final String path) {
        synchronized (this) {
            EscalatedResolver escalated = new EscalatedResolver(resourceResolverFactory, getClass());
            return escalated.doSession(new EscalatedResolver.Session<String>() {

                @Override
                public String run(ResourceResolver resolver) {
                    String normalizedPath = FilenameUtils.normalize(String.format("%s/%s/jcr:content", DIRECTORY, path), true);
                    Resource resource = resolver.getResource(normalizedPath);
                    ValueMap properties = ResourceUtil.getValueMap(resource);

                    if (comparePropertyValue(properties, "jcr:primaryType", "nt:resource") && PropertiesUtil.containsProperty(properties, "jcr:data")) {
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

    @Override
    public void clear() {
        synchronized (this) {
            EscalatedResolver escalated = new EscalatedResolver(resourceResolverFactory, getClass());
            escalated.doSession(new EscalatedResolver.Session<Boolean>() {

                @Override
                public Boolean run(ResourceResolver resolver) {
                    try {
                        Resource resource = ResourceUtil.getOrCreateResource(resolver, DIRECTORY, FOLDER_TYPE, FOLDER_TYPE, true);

                        if (resource != null) {
                            for (Resource child : resource.getChildren()) {
                                resolver.delete(child);
                            }
                        }

                        resolver.commit();
                    } catch (PersistenceException e) {
                        // TODO Log me
                    }

                    return true;
                }

            });
        }
    }

    @Override
    public void clear(Asset asset) {
        // TODO: Selective cache clear
    }

}
