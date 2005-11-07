package org.apache.commons.jci.compilers.groovy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.jci.compilers.AbstractJavaCompiler;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.tools.GroovyClass;

public final class GroovyJavaCompiler extends AbstractJavaCompiler {

    private final static Log log = LogFactory.getLog(GroovyJavaCompiler.class);
    
    public CompilationResult compile(
            final String[] clazzNames,
            final ResourceReader reader,
            final ResourceStore store
            ) {
     
        final CompilerConfiguration configuration = new CompilerConfiguration();
        final ClassLoader classloader = this.getClass().getClassLoader();
        final ErrorCollector collector = new ErrorCollector(configuration);
        final CompilationUnit unit = new CompilationUnit(configuration);
        final SourceUnit[] source = new SourceUnit[clazzNames.length];
        for (int i = 0; i < source.length; i++) {
            final String filename = clazzNames[i].replace('.','/') + ".groovy";
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
        
        final Collection problems = new ArrayList();

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
        } catch (final MultipleCompilationErrorsException e) {
            final ErrorCollector col = e.getErrorCollector();

            final Collection warnings = col.getWarnings();
            if (warnings != null) {
                for (final Iterator it = warnings.iterator(); it.hasNext();) {
                    final WarningMessage warning = (WarningMessage) it.next();
                    final CompilationProblem problem = new GroovyCompilationProblem(warning); 
                    if (problemHandler != null) {
                        problemHandler.handle(problem);
                    }
                    problems.add(problem);
                }
            }

            final Collection errors = col.getErrors();
            if (errors != null) {
                for (final Iterator it = errors.iterator(); it.hasNext();) {
                    final Message message = (Message) it.next();
                    final CompilationProblem problem = new GroovyCompilationProblem(message); 
                    if (problemHandler != null) {
                        problemHandler.handle(problem);
                    }
                    problems.add(problem);
                }
            }
        } catch (CompilationFailedException e) {
            e.printStackTrace();
            throw new RuntimeException("no expected");
        }
        
        return new CompilationResult(problems);
    }
}
