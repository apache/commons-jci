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

package org.apache.commons.jci;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.jci.classes.ExtendedDump;
import org.apache.commons.jci.classes.SimpleDump;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerSettings;
import org.apache.commons.jci.listeners.CompilingListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.problems.CompilationProblemHandler;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.utils.ConversionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author tcurdt
 */
public final class CompilingClassLoaderTestCase extends AbstractTestCase {

    private final Log log = LogFactory.getLog(CompilingClassLoaderTestCase.class);

    private ReloadingClassLoader classloader;
    private CompilingListener listener;
    private FilesystemAlterationMonitor fam;
        
    private final static class MockJavaCompiler implements JavaCompiler {

        private final Log log = LogFactory.getLog(MockJavaCompiler.class);

        public CompilationResult compile(String[] pResourcePaths, ResourceReader pReader, ResourceStore pStore, ClassLoader pClassLoader, JavaCompilerSettings pSettings ) {

            for (int i = 0; i < pResourcePaths.length; i++) {
                final String resourcePath = pResourcePaths[i];
                final byte[] resourceContent = pReader.getBytes(resourcePath);

                log.debug("resource " + resourcePath + " = " + ((resourceContent!=null)?new String(resourceContent):null) );

                final byte[] data;

                if ("jci/Simple.java".equals(resourcePath)) {

                    try {
                        data = SimpleDump.dump(new String(resourceContent));
                    } catch (Exception e) {
                        throw new RuntimeException("cannot handle resource " + resourcePath, e);
                    }

                } else if ("jci/Extended.java".equals(resourcePath)) {

                    try {
                        data = ExtendedDump.dump();
                    } catch (Exception e) {
                        throw new RuntimeException("cannot handle resource " + resourcePath, e);
                    }

                } else {
                    throw new RuntimeException("cannot handle resource " + resourcePath);
                }

                log.debug("compiling " + resourcePath + " (" + data.length + ")");

                pStore.write(ConversionUtils.stripExtension(resourcePath) + ".class", data);

            }

            return new CompilationResult(new CompilationProblem[0]);
        }

        public CompilationResult compile(String[] pResourcePaths, ResourceReader pReader, ResourceStore pStore, ClassLoader pClassLoader) {
            return compile(pResourcePaths, pReader, pStore, pClassLoader, null);
        }

        public CompilationResult compile(String[] pResourcePaths, ResourceReader pReader, ResourceStore pStore) {
            return compile(pResourcePaths, pReader, pStore, null);
        }

        public void setCompilationProblemHandler(CompilationProblemHandler pHandler) {
        }

        public JavaCompilerSettings createDefaultSettings() {
            return null;
        }

    }
    
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
                
        writeFile("jci/Simple.java", "Simple1");        
        writeFile("jci/Extended.java", "Extended");        
        
        log.debug("waiting for compile changes to get applied");        
        listener.waitForCheck();
        
        log.debug("*** ready to test");        
    }
    
    public void testCreate() throws Exception {
        initialCompile();
        
        log.debug("loading Simple");        
        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple1", simple.toString());
        
        log.debug("loading Extended");        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple1", extended.toString());
    }

    public void testChange() throws Exception {        
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple1", simple.toString());
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple1", extended.toString());

        delay();
        writeFile("jci/Simple.java", "Simple2");
        listener.waitForCheck();
    
        final Object simple2 = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple2", simple2.toString());
        
        final Object newExtended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple2", newExtended.toString());
    }

    public void testDelete() throws Exception {
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple1", simple.toString());
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple1", extended.toString());
                
        listener.waitForCheck();
        
        log.debug("deleting source file");
        assertTrue(new File(directory, "jci/Extended.java").delete());
        
        listener.waitForCheck();
       
        log.debug("loading Simple");
        final Object oldSimple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple1", oldSimple.toString());

        log.debug("trying to loading Extended");
        try {
            classloader.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertEquals("jci.Extended", e.getMessage());
        }        
        
        log.debug("deleting whole directory");
        FileUtils.deleteDirectory(new File(directory, "jci"));

        listener.waitForCheck();

        log.debug("trying to loading Simple");
        try {
            classloader.loadClass("jci.Simple").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertEquals("jci.Simple", e.getMessage());
        }

    }

    public void testDeleteDependency() throws Exception {        
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple1", simple.toString());
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple1", extended.toString());
        
        log.debug("deleting source file");
        assertTrue(new File(directory, "jci/Simple.java").delete());
        listener.waitForCheck();

        log.debug("trying to load dependend class");
        try {
            classloader.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final NoClassDefFoundError e) {
            assertEquals("jci/Simple", e.getMessage());
        }
        
    }

    protected void tearDown() throws Exception {
        fam.removeListener(listener);
        fam.stop();
        super.tearDown();
    }    
}
