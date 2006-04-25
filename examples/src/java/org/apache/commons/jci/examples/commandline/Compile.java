package org.apache.commons.jci.examples.commandline;

import org.apache.commons.jci.compilers.EclipseJavaCompiler;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerFactory;


public final class Compile {
    

    public static void main( String[] args ) {
        
        EclipseJavaCompiler jc = new EclipseJavaCompiler();
        
        final JavaCompiler compiler = JavaCompilerFactory.getInstance().createCompiler("eclipse");
        if (compiler == null) {
            System.out.println("Could not find compiler!");
        } else {
            System.out.println("Success!");
        }
    }
}
