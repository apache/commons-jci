package org.apache.commons.jci.compilers.groovy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.problems.CompilationProblemHandler;
import org.apache.commons.jci.problems.ConsoleCompilationProblemHandler;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;

public final class GroovyJavaCompiler implements JavaCompiler {

    private final static Log log = LogFactory.getLog(GroovyJavaCompiler.class);

    public void compile(
            final String[] clazzNames,
            final ResourceReader reader,
            final ResourceStore store,
            final CompilationProblemHandler problemHandler
            ) {
        throw new RuntimeException("NYI");
    }

    public static void main(String[] args) throws Exception {

        final JavaCompiler compiler = new GroovyJavaCompiler();
        final ConsoleCompilationProblemHandler problemHandler = new ConsoleCompilationProblemHandler();
        
        compiler.compile(
                args,
                new FileResourceReader("classes"),
                new MemoryResourceStore(),
                problemHandler
                );
        
        log.debug(
                problemHandler.getErrorCount() + " errors, " +
                problemHandler.getWarningCount() + " warnings"
                );
    }
}
