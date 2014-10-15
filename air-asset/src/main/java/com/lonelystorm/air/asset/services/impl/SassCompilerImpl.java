package com.lonelystorm.air.asset.services.impl;

import static com.lonelystorm.air.asset.util.PropertiesUtil.comparePropertyValue;
import static com.lonelystorm.air.asset.util.PropertiesUtil.containsProperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.osgi.OSGiScriptingContainer;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

import com.lonelystorm.air.asset.models.Asset;
import com.lonelystorm.air.asset.services.Compiler;
import com.lonelystorm.air.util.EscalatedResolver;

@Component
@Service
public class SassCompilerImpl implements Compiler {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private Bundle bundle;

    private volatile OSGiScriptingContainer container;

    @Activate
    public void activate(ComponentContext context) {
        bundle = context.getBundleContext().getBundle();
        container = new OSGiScriptingContainer(bundle, LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);

        final List<String> paths = container.getLoadPaths();
        paths.add("gems/sass-3.4.5/lib");
        container.setLoadPaths(paths);

        synchronized (this) {
            container.put("$service", this);
            container.runScriptlet(bundle, "scripts/setup.rb");
        }
    }

    @Override
    public String compile(Asset library, String file, String source) {
        synchronized (this) {
            container.put("$library", library);
            container.put("$content", source);
            container.put("$filename", library.getPath() + "/sass.scss");
            container.put("$loadPaths", library.getLoadPaths());
            container.runScriptlet(bundle, "scripts/compile.rb");

            return container.get("result").toString();
        }
    }

    @Override
    public boolean supports(Asset library, String file) {
        String filename = FilenameUtils.getBaseName(file);
        String extension = FilenameUtils.getExtension(file);

        if (!filename.startsWith("_") && (extension.equals("scss") || extension.equals("sass"))) {
            return true;
        }

        return false;
    }

    public String include(Asset library, String file) {
        String path = FilenameUtils.getPath(file);
        String filename = FilenameUtils.getBaseName(file);
        final String normalizedPath = FilenameUtils.normalize(String.format("/%s/_%s.scss/jcr:content", path, filename), true);

        EscalatedResolver escalated = new EscalatedResolver(resourceResolverFactory, getClass());
        String source = escalated.doSession(new EscalatedResolver.Session<String>() {

            @Override
            public String run(ResourceResolver resolver) {
                Resource resource = resolver.getResource(normalizedPath);
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

        return source;
    }

}
