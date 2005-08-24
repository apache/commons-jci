package org.apache.commons.jci.compilers.groovy;

import org.apache.commons.jci.compilers.AbstractCompilerTestCase;
import org.apache.commons.jci.compilers.GroovySources;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.problems.DefaultCompilationProblemHandler;


public final class GroovyJavaCompilerTestCase extends AbstractCompilerTestCase {
    public void testSimpleCompilation() throws Exception {
        final JavaCompiler compiler = new GroovyJavaCompiler();
        final DefaultCompilationProblemHandler handler = compileWith(compiler, GroovySources.simple);
        assertTrue(handler.getWarnings().length == 0);
        assertTrue(handler.getErrors().length == 0);
    }
    
    public void testCompilationError() throws Exception {
        final JavaCompiler compiler = new GroovyJavaCompiler();
        final DefaultCompilationProblemHandler handler = compileWith(compiler, GroovySources.error);
        assertTrue(handler.getWarnings().length == 0);
        assertTrue(handler.getErrors().length == 1);
    }

    public void testCompilationWarning() throws Exception {
        final JavaCompiler compiler = new GroovyJavaCompiler();
        final DefaultCompilationProblemHandler handler = compileWith(compiler, GroovySources.warning);
        assertTrue(handler.getWarnings().length == 1);
        assertTrue(handler.getErrors().length == 0);
    }}
