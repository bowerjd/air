package com.lonelystorm.air.asset.services.impl;

import static com.lonelystorm.air.asset.util.PropertiesUtil.comparePropertyValue;
import static com.lonelystorm.air.asset.util.PropertiesUtil.containsProperty;
import static org.apache.commons.lang.StringUtils.trim;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.jruby.RubyException;
import org.jruby.embed.InvokeFailedException;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.osgi.OSGiScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

import com.lonelystorm.air.asset.exceptions.CompilerException;
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

    private volatile Object receiver;

    @Activate
    public void activate(ComponentContext context) {
        bundle = context.getBundleContext().getBundle();
        container = new OSGiScriptingContainer(bundle, LocalContextScope.CONCURRENT, LocalVariableBehavior.TRANSIENT);

        final List<String> paths = container.getLoadPaths();
        paths.add("gems/sass-3.4.5/lib");
        container.setLoadPaths(paths);

        container.put("$service", this);
        receiver = container.runScriptlet(bundle, "scripts/setup.rb");
    }

    @Override
    public String compile(Asset library, String file, String source) throws CompilerException {
        try {
            return (String) container.callMethod(receiver, "compile", source, file, library.getLoadPaths());
        } catch (InvokeFailedException e) {
            RaiseException re = (RaiseException) e.getCause();
            RubyException ex = re.getException();

            String message = e.getMessage();
            String backtrace = ex.callMethod("sass_backtrace_str").asJavaString();

            throw new CompilerException(message, cause(message, backtrace));
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

    public String include(String file) {
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

    private CompilerException cause(String message, String backtrace) {
        String[] lines = StringUtils.split(backtrace, "\n");
        List<StackTraceElement> elements = new ArrayList<>();

        Pattern pattern = Pattern.compile("(on|from) line ([0-9]+) of (.+)");

        for (int i = 1; i < lines.length; i++) {
            String line = trim(lines[i]);
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                String file = matcher.group(3);
                int lineNumber = Integer.valueOf(matcher.group(2));

                StackTraceElement element = new StackTraceElement("SassCompiler", "compile", file, lineNumber);
                elements.add(element);
            }
        }

        CompilerException ex = new CompilerException(message);
        ex.setStackTrace(elements.toArray(new StackTraceElement[elements.size()]));

        return ex;
    }

}
