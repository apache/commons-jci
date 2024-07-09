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

package org.apache.commons.jci2.compiler.rhino;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jci2.core.compiler.CompilationResult;
import org.apache.commons.jci2.core.compiler.JavaCompiler;
import org.apache.commons.jci2.core.compilers.AbstractCompilerTestCase;
import org.apache.commons.jci2.core.readers.ResourceReader;
import org.apache.commons.jci2.core.stores.MemoryResourceStore;

/**
 *
 * @author tcurdt
 */
public final class RhinoJavaCompilerTestCase extends AbstractCompilerTestCase {

    @Override
    public JavaCompiler createJavaCompiler() {
        return new RhinoJavaCompiler();
    }

    @Override
    public String getCompilerName() {
        return "rhino";
    }

    @Override
    public void testSimpleCompile() throws Exception {
        final JavaCompiler compiler = createJavaCompiler();

        final ResourceReader reader = new ResourceReader() {
            final private Map<String, byte[]> sources = new HashMap<String, byte[]>() {
                private static final long serialVersionUID = 1L;
                {
                    put("jci2/Simple.js", (
                            " var i = 0;\n" +
                            "\n"
                    ).getBytes());
                }};

            @Override
            public byte[] getBytes( final String pResourceName ) {
                return sources.get(pResourceName);
            }

            @Override
            public boolean isAvailable( final String pResourceName ) {
                return sources.containsKey(pResourceName);
            }

        };

        final MemoryResourceStore store = new MemoryResourceStore();
        final CompilationResult result = compiler.compile(
                new String[] {
                        "jci2/Simple.js"
                }, reader, store);

        assertEquals(toString(result.getErrors()), 0, result.getErrors().length);
        assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);

        final byte[] clazzBytes = store.read("jci2/Simple.class");
        assertNotNull(clazzBytes);
        assertTrue(clazzBytes.length > 0);
    }

    @Override
    public void testExtendedCompile() throws Exception {
    }

    @Override
    public void testInternalClassCompile() throws Exception {
    }

    @Override
    public void testUppercasePackageNameCompile() throws Exception {
        final JavaCompiler compiler = createJavaCompiler();

        final ResourceReader reader = new ResourceReader() {
            final private Map<String, byte[]> sources = new HashMap<String, byte[]>() {
                private static final long serialVersionUID = 1L;
                {
                    put("Jci/Simple.js", (
                            " var i = 0;\n" +
                            "\n"
                    ).getBytes());
                }};

            @Override
            public byte[] getBytes( final String pResourceName ) {
                return sources.get(pResourceName);
            }

            @Override
            public boolean isAvailable( final String pResourceName ) {
                return sources.containsKey(pResourceName);
            }

        };

        final MemoryResourceStore store = new MemoryResourceStore();
        final CompilationResult result = compiler.compile(
                new String[] {
                        "Jci/Simple.js"
                }, reader, store);

        assertEquals(toString(result.getErrors()), 0, result.getErrors().length);
        assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);

        final byte[] clazzBytes = store.read("Jci/Simple.class");
        assertNotNull(clazzBytes);
        assertTrue(clazzBytes.length > 0);
    }

    @Override
    public void testCrossReferenceCompilation() throws Exception {
        // NA
    }

    @Override
    public void testAdditionalTopLevelClassCompile() throws Exception {
        // NA
    }

}
