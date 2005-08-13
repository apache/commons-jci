package org.apache.commons.jci.compilers.groovy;

import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.problems.CompilationProblemHandler;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
}
