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

package org.apache.commons.jci2.core;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.jci2.core.classes.ExtendedDump;
import org.apache.commons.jci2.core.classes.SimpleDump;
import org.apache.commons.jci2.core.compiler.CompilationResult;
import org.apache.commons.jci2.core.compiler.JavaCompiler;
import org.apache.commons.jci2.core.compiler.JavaCompilerSettings;
import org.apache.commons.jci2.core.listeners.CompilingListener;
import org.apache.commons.jci2.core.problems.CompilationProblem;
import org.apache.commons.jci2.core.problems.CompilationProblemHandler;
import org.apache.commons.jci2.core.readers.ResourceReader;
import org.apache.commons.jci2.core.stores.ResourceStore;
import org.apache.commons.jci2.core.utils.ConversionUtils;
import org.apache.commons.jci2.fam.monitor.FilesystemAlterationMonitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public final class CompilingClassLoaderTestCase extends AbstractTestCase {

    private final Log log = LogFactory.getLog(CompilingClassLoaderTestCase.class);

    private ReloadingClassLoader classloader;
    private CompilingListener listener;
    private FilesystemAlterationMonitor fam;

    private final static class MockJavaCompiler implements JavaCompiler {

        private final Log log = LogFactory.getLog(MockJavaCompiler.class);

        @Override
        public CompilationResult compile(final String[] pResourcePaths, final ResourceReader pReader, final ResourceStore pStore, final ClassLoader pClassLoader, final JavaCompilerSettings pSettings ) {

            for (final String resourcePath : pResourcePaths) {
                final byte[] resourceContent = pReader.getBytes(resourcePath);

                log.debug("resource " + resourcePath + " = " + (resourceContent!=null?new String(resourceContent):null) );

                final byte[] data;

                if ("jci2/Simple.java".equals(resourcePath)) {

                    try {
                        data = SimpleDump.dump(new String(resourceContent));
                    } catch (final Exception e) {
                        throw new IllegalArgumentException("cannot handle resource " + resourcePath, e);
                    }

                } else if ("jci2/Extended.java".equals(resourcePath)) {

                    try {
                        data = ExtendedDump.dump();
                    } catch (final Exception e) {
                        throw new IllegalArgumentException("cannot handle resource " + resourcePath, e);
                    }

                } else {
                    throw new IllegalArgumentException("cannot handle resource " + resourcePath);
                }

                log.debug("compiling " + resourcePath + " (" + data.length + ")");

                pStore.write(ConversionUtils.stripExtension(resourcePath) + ".class", data);

            }

            return new CompilationResult(new CompilationProblem[0]);
        }

        @Override
        public CompilationResult compile(final String[] pResourcePaths, final ResourceReader pReader, final ResourceStore pStore, final ClassLoader pClassLoader) {
            return compile(pResourcePaths, pReader, pStore, pClassLoader, null);
        }

        @Override
        public CompilationResult compile(final String[] pResourcePaths, final ResourceReader pReader, final ResourceStore pStore) {
            return compile(pResourcePaths, pReader, pStore, null);
        }

        @Override
        public void setCompilationProblemHandler(final CompilationProblemHandler pHandler) {
        }

        @Override
        public JavaCompilerSettings createDefaultSettings() {
            return null;
        }

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        classloader = new ReloadingClassLoader(this.getClass().getClassLoader());
        listener = new CompilingListener(new MockJavaCompiler());

        listener.addReloadNotificationListener(classloader);

        fam = new FilesystemAlterationMonitor();
        fam.addListener(directory, listener);
        fam.start();
    }

    private void initialCompile() throws Exception {
        log.debug("initial compile");

        listener.waitForFirstCheck();

        writeFile("jci2/Simple.java", "Simple1");
        writeFile("jci2/Extended.java", "Extended");

        log.debug("waiting for compile changes to get applied");
        listener.waitForCheck();

        log.debug("*** ready to test");
    }

    public void testCreate() throws Exception {
        initialCompile();

        log.debug("loading Simple");
        final Object simple = classloader.loadClass("jci2.Simple").getConstructor().newInstance();
        assertEquals("Simple1", simple.toString());

        log.debug("loading Extended");
        final Object extended = classloader.loadClass("jci2.Extended").getConstructor().newInstance();
        assertEquals("Extended:Simple1", extended.toString());
    }

    public void testChange() throws Exception {
        initialCompile();

        final Object simple = classloader.loadClass("jci2.Simple").getConstructor().newInstance();
        assertEquals("Simple1", simple.toString());

        final Object extended = classloader.loadClass("jci2.Extended").getConstructor().newInstance();
        assertEquals("Extended:Simple1", extended.toString());

        delay();
        writeFile("jci2/Simple.java", "Simple2");
        listener.waitForCheck();

        final Object simple2 = classloader.loadClass("jci2.Simple").getConstructor().newInstance();
        assertEquals("Simple2", simple2.toString());

        final Object newExtended = classloader.loadClass("jci2.Extended").getConstructor().newInstance();
        assertEquals("Extended:Simple2", newExtended.toString());
    }

    public void testDelete() throws Exception {
        initialCompile();

        final Object simple = classloader.loadClass("jci2.Simple").getConstructor().newInstance();
        assertEquals("Simple1", simple.toString());

        final Object extended = classloader.loadClass("jci2.Extended").getConstructor().newInstance();
        assertEquals("Extended:Simple1", extended.toString());

        listener.waitForCheck();

        log.debug("deleting source file");
        assertTrue(new File(directory, "jci2/Extended.java").delete());

        listener.waitForCheck();

        log.debug("loading Simple");
        final Object oldSimple = classloader.loadClass("jci2.Simple").getConstructor().newInstance();
        assertEquals("Simple1", oldSimple.toString());

        log.debug("trying to loading Extended");
        try {
            classloader.loadClass("jci2.Extended").getConstructor().newInstance();
            fail();
        } catch (final ClassNotFoundException e) {
            assertEquals("jci2.Extended", e.getMessage());
        }

        log.debug("deleting whole directory");
        FileUtils.deleteDirectory(new File(directory, "jci2"));

        listener.waitForCheck();

        log.debug("trying to loading Simple");
        try {
            classloader.loadClass("jci2.Simple").getConstructor().newInstance();
            fail();
        } catch (final ClassNotFoundException e) {
            assertEquals("jci2.Simple", e.getMessage());
        }

    }

    public void testDeleteDependency() throws Exception {
        initialCompile();

        final Object simple = classloader.loadClass("jci2.Simple").getConstructor().newInstance();
        assertEquals("Simple1", simple.toString());

        final Object extended = classloader.loadClass("jci2.Extended").getConstructor().newInstance();
        assertEquals("Extended:Simple1", extended.toString());

        log.debug("deleting source file");
        assertTrue(new File(directory, "jci2/Simple.java").delete());
        listener.waitForCheck();

        log.debug("trying to load dependend class");
        try {
            classloader.loadClass("jci2.Extended").getConstructor().newInstance();
            fail();
        } catch (final NoClassDefFoundError e) {
            assertEquals("jci2/Simple", e.getMessage());
        }

    }

    @Override
    protected void tearDown() throws Exception {
        fam.removeListener(listener);
        fam.stop();
        super.tearDown();
    }
}
