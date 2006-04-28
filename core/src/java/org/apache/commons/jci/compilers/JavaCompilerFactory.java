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

import java.util.HashMap;
import java.util.Map;



public final class JavaCompilerFactory {

    private Map classCache = new HashMap();
    
    private static final JavaCompilerFactory INSTANCE = new JavaCompilerFactory();
    
    public static JavaCompilerFactory getInstance() {
        return JavaCompilerFactory.INSTANCE;
    }

    private JavaCompilerFactory() {
    }
    

//    private Map getImplementations() {
//        final String[] jars = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
//        final Map implementations = new HashMap(jars.length);        
//        for (int i = 0; i < jars.length; i++) {
//            try {
//                final JarFile jar = new JarFile(jars[i]);
//                final Manifest manifest = jar.getManifest();
//                final String revision = (String) manifest.getMainAttributes().getValue("" + JavaCompiler.class);
//                implementations.put(jars[i], revision);
//            } catch (IOException e) {                
//            }
//        }
//
//      return implementations;
//    }
    
//    public JavaCompiler createCompiler(final JavaCompilerSettings pSettings) {
//        if (pSettings instanceof EclipseJavaCompilerSettings) {
//            return new EclipseJavaCompiler((EclipseJavaCompilerSettings) pSettings);
//        }
        
        // FIXME create settings for the other compilers and add here
        
//        return null;
//    }
    
    private String toJavaCasing(final String pName) {
        final char[] name = pName.toLowerCase().toCharArray();
        name[0] = Character.toUpperCase(name[0]);
        return new String(name);
    }
    /**
     * Can accept the following strings "eclipse", "janino", "groovy" and returns the appropriate
     * JavaCompiler. Return null for any other type of string.
     * 
     * @param compiler
     * @return
     */
    public JavaCompiler createCompiler(final String pHint) {
        
        final String className;
        if (pHint.indexOf('.') < 0) {
            className = "org.apache.commons.jci.compilers." + toJavaCasing(pHint) + "JavaCompiler";
        } else {
            className = pHint;
        }
        
        Class clazz = (Class) classCache.get(className);
        
        if (clazz == null) {
            try {
                clazz = Class.forName(className);
                classCache.put(className, clazz);
            } catch (ClassNotFoundException e) {
                clazz = null;
            }
        }

        if (clazz == null) {
            return null;
        }
        
        try {
            return (JavaCompiler) clazz.newInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
    
}
