/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jci.compilers;

import org.apache.commons.jci.compilers.eclipse.EclipseJavaCompiler;
import org.apache.commons.jci.compilers.eclipse.EclipseJavaCompilerSettings;
import org.apache.commons.jci.compilers.groovy.GroovyJavaCompiler;
import org.apache.commons.jci.compilers.janino.JaninoJavaCompiler;

public final class JavaCompilerFactory {
    
    public static final int ECLIPSE = 0;
    public static final int JANINO = 1;
    public static final int GROOVY = 2;
    
    private static final JavaCompilerFactory INSTANCE = new JavaCompilerFactory();
    
    public static JavaCompilerFactory getInstance() {
        return JavaCompilerFactory.INSTANCE;
    }
    
    private JavaCompilerFactory() {
    }
    
    public JavaCompiler createCompiler(final JavaCompilerSettings pSettings) {
        if (pSettings instanceof EclipseJavaCompilerSettings) {
            return new EclipseJavaCompiler((EclipseJavaCompilerSettings) pSettings);
        }
        
        // FIXME create settings for the other compilers and add here
        
        return null;
    }
    
    /**
     * Can accept the following strings "ECLIPSE", "JANINO", "GROOVY" and returns the appropriate
     * JavaCompiler. Return null for any other type of string.
     * 
     * @param compiler
     * @return
     */
    public JavaCompiler createCompiler(final String pHint) {
        if ("eclipse".equalsIgnoreCase(pHint)) {
            return createCompiler(ECLIPSE);
        }
        if ("janino".equalsIgnoreCase(pHint)) {
            return createCompiler(JANINO);
        }
        if ("groovy".equalsIgnoreCase(pHint)) {
            return createCompiler(GROOVY);
        }
    
        return null;                       
    }
    
    /**
     * Can accept the following ints
     *  JavaCompilerFactory.ECLIPSE
     *  JavaCompilerFactory.JANINO
     *  JavaCompilerFactory.GROOVY 
     * and returns the appropriate JavaCompiler. Return null for any other int.
     * 
     * @param compiler
     * @return
     */    
    public JavaCompiler createCompiler(final int pCompiler) {
        switch (pCompiler) {
            case ECLIPSE:
                return new EclipseJavaCompiler();
            case JANINO:
                return new JaninoJavaCompiler();
            case GROOVY:
                return new GroovyJavaCompiler();
            default:
                return null;
        }
    }
}