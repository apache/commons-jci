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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;

/**
 * Providing convenience methods for JavaCompiler TestCases
 *
 * @author tcurdt
 */
public abstract class AbstractCompilerTestCase extends TestCase {

    public abstract JavaCompiler createJavaCompiler();

    public abstract String getCompilerName();

    public void testFactoryCreation() {
        final JavaCompiler factoryCompiler = new JavaCompilerFactory().createCompiler(getCompilerName());
        assertNotNull(factoryCompiler);

        final JavaCompiler compiler = createJavaCompiler();
        assertEquals(factoryCompiler.getClass().getName(), compiler.getClass().getName());
    }

    public void testSimpleCompile() throws Exception {
        final JavaCompiler compiler = createJavaCompiler();

        final ResourceReader reader = new ResourceReader() {
            final private Map sources = new HashMap() {
                private static final long serialVersionUID = 1L;
                {
                    put("jci/Simple.java", (
                        "package jci;\n" +
                        "public class Simple {\n" +
                        "  public String toString() {\n" +
                        "    return \"Simple\";\n" +
                        "  }\n" +
                        "}").getBytes());
                }};

            public byte[] getBytes( final String pResourceName ) {
                return (byte[]) sources.get(pResourceName);
            }

            public boolean isAvailable( final String pResourceName ) {
                return sources.containsKey(pResourceName);
            }

        };

        final MemoryResourceStore store = new MemoryResourceStore();
        final CompilationResult result = compiler.compile(
                new String[] {
                        "jci/Simple.java"
                }, reader, store);

        assertEquals(toString(result.getErrors()), 0, result.getErrors().length);
        assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);

        final byte[] clazzBytes = store.read("jci/Simple.class");
        assertNotNull("jci/Simple.class is not null",clazzBytes);
        assertTrue("jci/Simple.class is not empty", clazzBytes.length > 0);
    }

    public void testExtendedCompile() throws Exception {
        final JavaCompiler compiler = createJavaCompiler();

        final ResourceReader reader = new ResourceReader() {
            final private Map sources = new HashMap() {
                private static final long serialVersionUID = 1L;
                {
                    put("jci/Simple.java", (
                            "package jci;\n" +
                            "public class Simple {\n" +
                            "  public String toString() {\n" +
                            "    return \"Simple\";\n" +
                            "  }\n" +
                    "}").getBytes());
                    put("jci/Extended.java", (
                            "package jci;\n" +
                            "public class Extended extends Simple {\n" +
                            "  public String toString() {\n" +
                            "    return \"Extended\" + super.toString();\n" +
                            "  }\n" +
                    "}").getBytes());
                }};

            public byte[] getBytes( final String pResourceName ) {
                return (byte[]) sources.get(pResourceName);
            }

            public boolean isAvailable( final String pResourceName ) {
                return sources.containsKey(pResourceName);
            }

        };

        final MemoryResourceStore store = new MemoryResourceStore();
        final CompilationResult result = compiler.compile(
                new String[] {
                        "jci/Extended.java",
                        "jci/Simple.java"
                }, reader, store);

        assertEquals(toString(result.getErrors()), 0, result.getErrors().length);
        assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);

        final byte[] clazzBytesSimple = store.read("jci/Simple.class");
        assertNotNull("jci/Simple.class is not null", clazzBytesSimple);
        assertTrue("jci/Simple.class is not empty", clazzBytesSimple.length > 0);

        final byte[] clazzBytesExtended = store.read("jci/Extended.class");
        assertNotNull("jci/Extended.class is not null", clazzBytesExtended);
        assertTrue("jci/Extended.class is not empty",clazzBytesExtended.length > 0);
    }

    public void testInternalClassCompile() throws Exception {
        final JavaCompiler compiler = createJavaCompiler();

        final ResourceReader reader = new ResourceReader() {
            final private Map sources = new HashMap() {
                private static final long serialVersionUID = 1L;
                {
                    put("jci/Simple.java", (
                            "package jci;\n" +
                            "public class Simple {\n" +
                            "  private class Sub {\n" +
                            "  }\n" +
                            "  public String toString() {\n" +
                            "    new Sub();\n" +
                            "    return \"Simple\";\n" +
                            "  }\n" +
                    "}").getBytes());
                }};

            public byte[] getBytes( final String pResourceName ) {
                return (byte[]) sources.get(pResourceName);
            }

            public boolean isAvailable( final String pResourceName ) {
                return sources.containsKey(pResourceName);
            }

        };

        final MemoryResourceStore store = new MemoryResourceStore();
        final CompilationResult result = compiler.compile(
                new String[] {
                        "jci/Simple.java"
                }, reader, store);

        assertEquals(toString(result.getErrors()), 0, result.getErrors().length);
        assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);

        final byte[] clazzBytes = store.read("jci/Simple.class");
        assertNotNull("jci/Simple.class is not null", clazzBytes);
        assertTrue("jci/Simple.class is not empty", clazzBytes.length > 0);

        final byte[] subClazzBytes = store.read("jci/Simple$Sub.class");
        assertNotNull("jci/Simple$Sub.class is not null", subClazzBytes);
        assertTrue("jci/Simple$Sub.class is not empty", subClazzBytes.length > 0);

    }

    public void testUppercasePackageNameCompile() throws Exception {
        final JavaCompiler compiler = createJavaCompiler();

        final ResourceReader reader = new ResourceReader() {
            final private Map sources = new HashMap() {
                private static final long serialVersionUID = 1L;
                {
                    put("Jci/Simple.java", (
                            "package Jci;\n" +
                            "public class Simple {\n" +
                            "  public String toString() {\n" +
                            "    return \"Simple\";\n" +
                            "  }\n" +
                    "}").getBytes());
                }};

            public byte[] getBytes( final String pResourceName ) {
                return (byte[]) sources.get(pResourceName);
            }

            public boolean isAvailable( final String pResourceName ) {
                return sources.containsKey(pResourceName);
            }

        };

        final MemoryResourceStore store = new MemoryResourceStore();
        final CompilationResult result = compiler.compile(
                new String[] {
                        "Jci/Simple.java"
                }, reader, store);

        assertEquals(toString(result.getErrors()), 0, result.getErrors().length);
        assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);

        final byte[] clazzBytes = store.read("Jci/Simple.class");
        assertNotNull("Jci/Simple.class is not null", clazzBytes);
        assertTrue("Jci/Simple.class is not empty", clazzBytes.length > 0);
    }

    /*
     * https://issues.apache.org/jira/browse/JCI-53
     */
    public void testCrossReferenceCompilation() throws Exception {
      final String javaVersion = System.getProperty("java.version");

      if (!(javaVersion.startsWith("1.5") || javaVersion.startsWith("1.6"))) {
          System.err.println("WARNING! Skipping testCrossReferenceCompilation() because your runtime does not support java 1.5+ yet");
          return;
      }

      final JavaCompiler compiler = createJavaCompiler();

        final ResourceReader reader = new ResourceReader() {
            final private Map sources = new HashMap() {
                private static final long serialVersionUID = 1L;
                {
                    put("jci/Func1.java", (
                            "package jci;\n" +
                            "import static jci.Func2.func2;" +
                            "public class Func1 {\n" +
                            "  public static boolean func1() throws Exception {\n" +
                            "    return true;\n" +
                            "  }\n" +
                    "}").getBytes());
                    put("jci/Func2.java", (
                            "package jci;\n" +
                            "import static jci.Func1.func1;" +
                            "public class Func2 {\n" +
                            "  public static boolean func2() throws Exception {\n" +
                            "    return true;\n" +
                            "  }\n" +
                    "}").getBytes());
                }};

            public byte[] getBytes( final String pResourceName ) {
                return (byte[]) sources.get(pResourceName);
            }

            public boolean isAvailable( final String pResourceName ) {
                return sources.containsKey(pResourceName);
            }

        };

        final JavaCompilerSettings settings = compiler.createDefaultSettings();
        settings.setTargetVersion("1.5");
        settings.setSourceVersion("1.5");

        final MemoryResourceStore store = new MemoryResourceStore();
        final CompilationResult result = compiler.compile(
                new String[] {
                        "jci/Func1.java",
                        "jci/Func2.java"
                }, reader, store, this.getClass().getClassLoader(), settings);

        assertEquals(toString(result.getErrors()), 0, result.getErrors().length);
        assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);

        final byte[] clazzBytesFunc1 = store.read("jci/Func1.class");
        assertNotNull("jci/Func1.class is not null", clazzBytesFunc1);
        assertTrue("jci/Func1.class is not empty", clazzBytesFunc1.length > 0);

        final byte[] clazzBytesFunc2 = store.read("jci/Func2.class");
        assertNotNull("jci/Func2.class is not null", clazzBytesFunc2);
        assertTrue("jci/Func2.class is not empty", clazzBytesFunc2.length > 0);
    }

    /*
     * https://issues.apache.org/jira/browse/JCI-59
     */
    public void testAdditionalTopLevelClassCompile() throws Exception {
        final JavaCompiler compiler = createJavaCompiler(); 
    
        final ResourceReader reader = new ResourceReader() {
           final private Map sources = new HashMap() {
               private static final long serialVersionUID = 1L;
               {
                   put("jci/Simple.java", (
                       "package jci;\n" +
                       "public class Simple {\n" +
                       "  public String toString() {\n" +
                       "    return \"Simple\";\n" +
                       "  }\n" +
                       "}\n" +
                       "class AdditionalTopLevel {\n" +
                       "  public String toString() {\n" +
                       "    return \"AdditionalTopLevel\";\n" +
                       "  }\n" +
                       "}").getBytes());
               }};
    
           public byte[] getBytes( final String pResourceName ) {
               return (byte[]) sources.get(pResourceName);
           }
    
           public boolean isAvailable( final String pResourceName ) {
               return sources.containsKey(pResourceName);
           }
        };
    
        final MemoryResourceStore store = new MemoryResourceStore();
        final CompilationResult result = compiler.compile(
               new String[] {
                       "jci/Simple.java"
               }, reader, store);
    
        assertEquals(toString(result.getErrors()), 0, result.getErrors().length);
        assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);
    
        final byte[] clazzBytes = store.read("jci/Simple.class");
        assertNotNull(clazzBytes);
        assertTrue(clazzBytes.length > 0);
    }

    public final String toString( final CompilationProblem[] pProblems ) {
        final StringBuffer sb = new StringBuffer();

        for (int i = 0; i < pProblems.length; i++) {
            final CompilationProblem problem = pProblems[i];
            sb.append(problem.getMessage()).append(", ");
        }

        return sb.toString();
    }

}
