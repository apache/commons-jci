package org.apache.commons.jci.compilers;

import org.apache.commons.jci.problems.CompilationProblemHandler;


public abstract class AbstractJavaCompiler implements JavaCompiler {

    protected CompilationProblemHandler problemHandler;
    
    public void setCompilationProblemHandler( final CompilationProblemHandler pHandler ) {
        problemHandler = pHandler;
    }
    
}
