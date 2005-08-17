package org.apache.commons.jci.compilers.groovy;

import org.apache.commons.jci.compilers.AbstractCompilerTestCase;
import org.apache.commons.jci.compilers.GroovySources;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.problems.CompilationProblemHandler;


public final class GroovyJavaCompilerTestCase extends AbstractCompilerTestCase {
    public void testSimpleCompilation() throws Exception {
        final JavaCompiler compiler = new GroovyJavaCompiler();
        final CompilationProblemHandler handler = compileWith(compiler, GroovySources.simple);
        assertTrue(handler.getWarningCount() == 0);
        assertTrue(handler.getErrorCount() == 0);
    }
    
    public void testCompilationError() throws Exception {
        final JavaCompiler compiler = new GroovyJavaCompiler();
        final CompilationProblemHandler handler = compileWith(compiler, GroovySources.error);
        assertTrue(handler.getWarningCount() == 0);
        assertTrue(handler.getErrorCount() == 1);
    }

    public void testCompilationWarning() throws Exception {
        final JavaCompiler compiler = new GroovyJavaCompiler();
        final CompilationProblemHandler handler = compileWith(compiler, GroovySources.warning);
        assertTrue(handler.getWarningCount() == 1);
        assertTrue(handler.getErrorCount() == 0);
    }}
