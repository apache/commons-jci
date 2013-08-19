/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.StringBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.vafer.dependency.asm.RenamingVisitor;
import org.vafer.dependency.utils.ResourceRenamer;


/**
 * This classloader injects the FileIn/OutputStream wrappers
 * into javac. Access to the store/reader is done via ThreadLocals.
 * It also tries to find the javac class on the system and expands
 * the classpath accordingly.
 * 
 * @author tcurdt
 */
public final class JavacClassLoader extends URLClassLoader {

    private final Map loaded = new HashMap();

    public JavacClassLoader( final ClassLoader pParent ) {
        super(getToolsJar(), pParent);
    }

    private static URL[] getToolsJar() {
        try {
            Class.forName("com.sun.tools.javac.Main");

            // found - no addtional classpath entry required
            return new URL[0];

        } catch (Exception e) {
        }

        // no compiler in current classpath, let's try to find the tools.jar

        String javaHome = System.getProperty("java.home");
        if (javaHome.toLowerCase(Locale.US).endsWith(File.separator + "jre")) {
            javaHome = javaHome.substring(0, javaHome.length()-4);
        }

        final File toolsJar = new File(javaHome + "/lib/tools.jar");

        if (toolsJar.exists()) {
            try {
                return new URL[] { toolsJar.toURL() };
            } catch (MalformedURLException e) {
            }
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("Could not find javac compiler class (should be in the tools.jar/classes.jar in your JRE/JDK). ");
        sb.append("os.name").append('=').append(System.getProperty("os.name")).append(", ");
        sb.append("os.version").append('=').append(System.getProperty("os.version")).append(", ");
        sb.append("java.class.path").append('=').append(System.getProperty("java.class.path"));

        throw new RuntimeException(sb.toString());
    }

    protected Class findClass( final String name ) throws ClassNotFoundException {

        if (name.startsWith("java.")) {
            return super.findClass(name);
        }

        try {

            final Class clazz = (Class) loaded.get(name);
            if (clazz != null) {
                return clazz;
            }

            final byte[] classBytes;

            if (name.startsWith("com.sun.tools.javac.")) {
                final InputStream classStream = getResourceAsStream(name.replace('.', '/') + ".class");

                final ClassWriter renamedCw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                new ClassReader(classStream).accept(new RenamingVisitor(new CheckClassAdapter(renamedCw), new ResourceRenamer() {
                    public String getNewNameFor(final String pOldName) {
                        if (pOldName.startsWith(FileOutputStream.class.getName())) {
                            return FileOutputStreamProxy.class.getName();
                        }
                        if (pOldName.startsWith(FileInputStream.class.getName())) {
                            return FileInputStreamProxy.class.getName();
                        }
                        return pOldName;
                    }
                }), 0); // We don't set ClassReader.SKIP_DEBUG

                classBytes = renamedCw.toByteArray();

            } else {
                return super.findClass(name);
            }

            final Class newClazz = defineClass(name, classBytes, 0, classBytes.length);
            loaded.put(name, newClazz);
            return newClazz;
        } catch (IOException e) {
            throw new ClassNotFoundException("", e);
        }
    }

    protected synchronized Class loadClass( final String classname, final boolean resolve ) throws ClassNotFoundException {

        Class theClass = findLoadedClass(classname);
        if (theClass != null) {
            return theClass;
        }

        try {
            theClass = findClass(classname);
        } catch (ClassNotFoundException cnfe) {
            theClass = getParent().loadClass(classname);
        }

        if (resolve) {
            resolveClass(theClass);
        }

        return theClass;
    }
}