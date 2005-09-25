package org.apache.commons.jci.compilers.eclipse;

import org.apache.commons.jci.compilers.AbstractCompilerTestCase;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaSources;


public final class EclipseJavaCompilerTestCase extends AbstractCompilerTestCase {
    public void testSimpleCompilation() throws Exception {
        final JavaCompiler compiler = new EclipseJavaCompiler();
        final CompilationResult result = compileWith(compiler, JavaSources.simple);
        assertTrue(result.getWarnings().length == 0);
        assertTrue(result.getErrors().length == 0);
    }
    
    public void testCompilationError() throws Exception {
        final JavaCompiler compiler = new EclipseJavaCompiler();
        final CompilationResult result = compileWith(compiler, JavaSources.error);
        assertTrue(result.getWarnings().length == 0);
        assertTrue(result.getErrors().length == 1);
    }

    public void testCompilationWarning() throws Exception {
        final JavaCompiler compiler = new EclipseJavaCompiler();
        final CompilationResult result = compileWith(compiler, JavaSources.warning1);
        assertTrue(result.getWarnings().length == 1);
        assertTrue(result.getErrors().length == 0);
   }    
}
