package org.apache.commons.jci.compilers.groovy;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.jci.compilers.JavaCompiler;
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
     
        final CompilerConfiguration configuration = new CompilerConfiguration();
        final ClassLoader classloader = this.getClass().getClassLoader();
        final ErrorCollector collector = new ErrorCollector(configuration);
        final CompilationUnit unit = new CompilationUnit(configuration);
        final SourceUnit[] source = new SourceUnit[clazzNames.length];
        for (int i = 0; i < source.length; i++) {
            final String filename = clazzNames[i].replace('.','/') + ".java";
            log.debug("adding source unit " + filename);
            source[i] = new SourceUnit(
                    filename,
                    new String(reader.getContent(filename)), // FIXME delay the read
                    configuration,
                    classloader,
                    collector
                    );
            unit.addSource(source[i]);
        }
        try {
            log.debug("compiling");
            unit.compile();
            
            final List classes = unit.getClasses();
            for (final Iterator it = classes.iterator(); it.hasNext();) {
                final GroovyClass clazz = (GroovyClass) it.next();
                final String name = clazz.getName().replace('.', File.separatorChar) + ".class";
                final byte[] bytes = clazz.getBytes();
                store.write(name, bytes);
            }
        } catch (final CompilationFailedException e) {
            final ErrorCollector col = e.getUnit().getErrorCollector();
            
            final List warnings = col.getWarnings();
            log.debug("handling " + warnings.size() + " warnings");
            for (final Iterator it = warnings.iterator(); it.hasNext();) {
                final WarningMessage warning = (WarningMessage) it.next();
                problemHandler.handle(
                        new GroovyCompilationProblem(warning)
                        );
            }

            final List errors = col.getErrors();
            log.debug("handling " + errors.size() + " errors");
            for (final Iterator it = errors.iterator(); it.hasNext();) {
                final Message message = (Message) it.next();
                problemHandler.handle(
                        new GroovyCompilationProblem(message)
                        );                
            }
        }
        
    }
}
