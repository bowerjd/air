package com.lonelystorm.air.asset.services.impl;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.FilenameUtils.getPathNoEndSeparator;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.trim;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
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
import com.lonelystorm.air.asset.models.AssetLibrary;
import com.lonelystorm.air.asset.services.Compiler;
import com.lonelystorm.air.asset.services.FileResolver;
import com.lonelystorm.air.asset.services.LibraryResolver;

@Component
@Service
public class SassCompilerImpl implements Compiler {

    private static final String[] FILE_FORMATS = {
        "/%s/_%s.scss/jcr:content",
        "/%s/%s.scss/jcr:content"
    };

    @Reference
    private FileResolver fileResolver;

    @Reference
    private LibraryResolver libraryResolver;

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
            // See http://www.sass-lang.com/documentation/file.SASS_REFERENCE.html
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
        String filename = getBaseName(file);
        String extension = getExtension(file);

        if (!filename.startsWith("_") && (extension.equals("scss") || extension.equals("sass"))) {
            return true;
        }

        return false;
    }

    // TODO: Rework
    public String include(String directory, String filename) {
        String location = format("%s/%s", directory, removeStart(filename, "/"));

        directory = getPathNoEndSeparator(location);
        filename = getName(location);

        for (String format : FILE_FORMATS) {
            location = format(format, directory, getBaseName(filename));

            if (fileResolver.exists(location)) {
                return fileResolver.load(location);
            }
        }

        // Else
        StringBuilder files = new StringBuilder();
        List<AssetLibrary> libraries = libraryResolver.findLibrariesByCategory(filename);
        if (libraries != null) {
            for (AssetLibrary library : libraries) {
                Set<String> sources = library.getSources();
                for (String source : sources) {
                    if (supports(null, source)) {
                        files.append(String.format("@import '%s';", source));
                    }
                }
            }
        }

        if (files.length() == 0) {
            return null;
        } else {
            return files.toString();
        }
    }

    private CompilerException cause(String message, String backtrace) {
        String[] lines = split(backtrace, "\n");
        List<StackTraceElement> elements = new ArrayList<>();

        Pattern pattern = Pattern.compile("(on|from) line ([0-9]+) of (.+)");

        for (int i = 1; i < lines.length; i++) {
            String line = trim(lines[i]);
            Matcher matcher = pattern.matcher(line);

            if (matcher.matches()) {
                String file = matcher.group(3);
                int lineNumber = parseInt(matcher.group(2));

                StackTraceElement element = new StackTraceElement("SassCompiler", "compile", file, lineNumber);
                elements.add(element);
            }
        }

        CompilerException ex = new CompilerException(message);
        ex.setStackTrace(elements.toArray(new StackTraceElement[elements.size()]));

        return ex;
    }

}
