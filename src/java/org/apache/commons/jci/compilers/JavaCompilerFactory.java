package org.apache.commons.jci.compilers;

public interface JavaCompilerFactory {
    
    JavaCompiler createCompiler(final Object pHint);

}
