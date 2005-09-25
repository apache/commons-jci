package org.apache.commons.jci.compilers.groovy;

import org.apache.commons.jci.compilers.AbstractCompilerTestCase;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.GroovySources;
import org.apache.commons.jci.compilers.JavaCompiler;


public final class GroovyJavaCompilerTestCase extends AbstractCompilerTestCase {
    public void testSimpleCompilation() throws Exception {
        final JavaCompiler compiler = new GroovyJavaCompiler();
        final CompilationResult result = compileWith(compiler, GroovySources.simple);
        assertTrue(result.getWarnings().length == 0);
        assertTrue(result.getErrors().length == 0);
    }
    
    public void testCompilationError() throws Exception {
        final JavaCompiler compiler = new GroovyJavaCompiler();
        final CompilationResult result = compileWith(compiler, GroovySources.error);
        assertTrue(result.getWarnings().length == 0);
        assertTrue(result.getErrors().length == 1);
    }

// as for now Groovy does not support any warnings at all
//    public void testCompilationWarning() throws Exception {
//        final JavaCompiler compiler = new GroovyJavaCompiler();
//        final CompilationResult result = compileWith(compiler, GroovySources.warning);
//        assertTrue(result.getWarnings().length == 1);
//        assertTrue(result.getErrors().length == 0);
//    }
}
