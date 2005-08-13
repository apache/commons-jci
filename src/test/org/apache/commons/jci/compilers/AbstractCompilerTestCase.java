package org.apache.commons.jci.compilers;

import java.io.File;
import org.apache.commons.jci.AbstractTestCase;
import org.apache.commons.jci.problems.CompilationProblemHandler;
import org.apache.commons.jci.problems.LogCompilationProblemHandler;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.stores.FileResourceStore;


public abstract class AbstractCompilerTestCase extends AbstractTestCase {

        
    protected CompilationProblemHandler compileWith( final JavaCompiler pCompiler, final String pSource ) throws Exception {
        final File srcDir = new File(directory, "src");
        final File dstDir = new File(directory, "dst");
        
        assertTrue(srcDir.mkdir());
        assertTrue(dstDir.mkdir());
        
        final FileResourceReader src = new FileResourceReader(srcDir);
        final FileResourceStore dst = new FileResourceStore(dstDir);
        
        final CompilationProblemHandler handler = new LogCompilationProblemHandler();

        writeFile("src/jci/Simple.java", pSource);
        
        pCompiler.compile(
                new String[] { "jci.Simple"},
                src,
                dst,
                handler
                );
        
        return handler;
    }
}
