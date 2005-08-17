package org.apache.commons.jci.compilers.eclipse;

import org.apache.commons.jci.compilers.AbstractCompilerTestCase;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaSources;
import org.apache.commons.jci.problems.CompilationProblemHandler;


public final class EclipseJavaCompilerTestCase extends AbstractCompilerTestCase {
    public void testSimpleCompilation() throws Exception {
        final JavaCompiler compiler = new EclipseJavaCompiler();
        final CompilationProblemHandler handler = compileWith(compiler, JavaSources.simple);
        assertTrue(handler.getWarningCount() == 0);
        assertTrue(handler.getErrorCount() == 0);
    }
    
    public void testCompilationError() throws Exception {
        final JavaCompiler compiler = new EclipseJavaCompiler();
        final CompilationProblemHandler handler = compileWith(compiler, JavaSources.error);
        assertTrue(handler.getWarningCount() == 0);
        assertTrue(handler.getErrorCount() == 1);
    }

    public void testCompilationWarning() throws Exception {
        final JavaCompiler compiler = new EclipseJavaCompiler();
        final CompilationProblemHandler handler = compileWith(compiler, JavaSources.warning);
        assertTrue(handler.getWarningCount() == 1);
        assertTrue(handler.getErrorCount() == 0);
    }    
}
