package org.apache.jci.compilers.groovy;

import org.apache.jci.compilers.JavaCompiler;
import org.apache.jci.problems.CompilationProblemHandler;
import org.apache.jci.problems.ConsoleCompilationProblemHandler;
import org.apache.jci.readers.FileResourceReader;
import org.apache.jci.readers.ResourceReader;
import org.apache.jci.stores.MemoryResourceStore;
import org.apache.jci.stores.ResourceStore;

public final class GroovyJavaCompiler implements JavaCompiler {
    
    public void compile(
            final String[] clazzNames,
            final ResourceReader reader,
            final ResourceStore store,
            final CompilationProblemHandler problemHandler
            ) {

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
        
        System.out.println(
                problemHandler.getErrorCount() + " errors, " +
                problemHandler.getWarningCount() + " warnings"
                );
    }
}
