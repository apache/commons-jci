package org.apache.commons.jci.compilers.groovy;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.problems.CompilationProblemHandler;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.tools.GroovyClass;

public final class GroovyJavaCompiler implements JavaCompiler {

    private final static Log log = LogFactory.getLog(GroovyJavaCompiler.class);
    
    public void compile(
            final String[] clazzNames,
            final ResourceReader reader,
            final ResourceStore store,
            final CompilationProblemHandler problemHandler
            ) {
     
        final ClassLoader classloader = null;
        final ErrorCollector collector = null;
        final CompilerConfiguration configuration = null;
        final CompilationUnit unit = null;
        final SourceUnit[] source = new SourceUnit[clazzNames.length];
        for (int i = 0; i < source.length; i++) {
            source[i] = new SourceUnit(
                    clazzNames[i],
                    new String(reader.getContent(clazzNames[i])),
                    configuration,
                    classloader,
                    collector
                    );
            unit.addSource(source[i]);
        }
        try {
            unit.compile();
            
            final List classes = unit.getClasses();
            for (final Iterator it = classes.iterator(); it.hasNext();) {
                final GroovyClass clazz = (GroovyClass) it.next();
                final String name = clazz.getName().replace('.', File.separatorChar) + ".class";
                byte[] bytes = clazz.getBytes();
                store.write(name, bytes);
            }
        } catch (final CompilationFailedException e) {
            final ErrorCollector errorC = e.getUnit().getErrorCollector();
            
            final List warnings = errorC.getWarnings();
            for (final Iterator it = warnings.iterator(); it.hasNext();) {
                final WarningMessage warning = (WarningMessage) it.next();
                problemHandler.handle(
                        new CompilationProblem(0, "", warning.getMessage(), 0, 0, false)
                        );
            }

            final List errors = errorC.getErrors();
            for (final Iterator it = errors.iterator(); it.hasNext();) {
                final Message message = (Message) it.next();
                problemHandler.handle(
                        new CompilationProblem(0, "", "", 0, 0, false)
                        );                
            }
        }
        
    }
}
