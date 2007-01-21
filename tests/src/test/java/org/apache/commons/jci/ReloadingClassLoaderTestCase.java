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
import org.apache.commons.jci.compilers.JavaSources;
import org.apache.commons.jci.listeners.ReloadingListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class ReloadingClassLoaderTestCase extends AbstractTestCase {

    private final Log log = LogFactory.getLog(ReloadingClassLoaderTestCase.class);
    
    private ReloadingClassLoader classloader;
    private ReloadingListener listener;
    private FilesystemAlterationMonitor fam;

    private final byte[] clazzSimple;
    private final byte[] clazzSIMPLE;
    private final byte[] clazzExtended;
    
    public ReloadingClassLoaderTestCase() {
        clazzSimple = CompilerUtils.compile("jci.Simple", JavaSources.simple);
        clazzSIMPLE = CompilerUtils.compile("jci.Simple", JavaSources.SIMPLE);
        clazzExtended = CompilerUtils.compile(
                new String[] { "jci.Extended", "jci.Simple" },
                new String[] { JavaSources.extended, JavaSources.simple }
                );
        assertTrue(clazzSimple.length > 0);
        assertTrue(clazzSIMPLE.length > 0);
        assertTrue(clazzExtended.length > 0);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        classloader = new ReloadingClassLoader(this.getClass().getClassLoader());
        listener = new ReloadingListener(directory);
        
        // listener.addListener(classloader);
        classloader.addListener(listener);
        
        fam = new FilesystemAlterationMonitor();
        fam.addListener(listener);
        fam.start();
    }

    public void testCreate() throws Exception {
        listener.waitForCheck();

        log.debug("creating class");
        
        delay();
        writeFile("jci/Simple.class", clazzSimple);
        listener.waitForCheck();
        
        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple", simple.toString());        
    }

    public void testChange() throws Exception {        
        listener.waitForCheck();

        log.debug("creating class");

        delay();        
        writeFile("jci/Simple.class", clazzSimple);
        listener.waitForCheck();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple", simple.toString());
        
        log.debug("changing class");
        
        delay();        
        writeFile("jci/Simple.class", clazzSIMPLE);
        listener.waitForEvent();
    
        final Object SIMPLE = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("SIMPLE", SIMPLE.toString());        
    }

    public void testDelete() throws Exception {
        listener.waitForCheck();

        log.debug("creating class");

        delay();        
        writeFile("jci/Simple.class", clazzSimple);
        listener.waitForCheck();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple", simple.toString());

        log.debug("deleting class");
        
        assertTrue(new File(directory, "jci/Simple.class").delete());
        
        listener.waitForEvent();

        try {
            classloader.loadClass("jci.Simple").newInstance();        
            fail();
        } catch(final ClassNotFoundException e) {
            assertEquals("jci.Simple", e.getMessage());
        }        
    }

    public void testDeleteDependency() throws Exception {        
        listener.waitForCheck();

        log.debug("creating classes");

        delay();        
        writeFile("jci/Simple.class", clazzSimple);
        writeFile("jci/Extended.class", clazzExtended);
        listener.waitForCheck();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple", simple.toString());
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple", extended.toString());

        log.debug("deleting class dependency");
        
        assertTrue(new File(directory, "jci/Simple.class").delete());
        
        listener.waitForEvent();

        try {
            classloader.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final NoClassDefFoundError e) {
            assertEquals("jci/Simple", e.getMessage());
        }
    }

    public void testClassNotFound() {
        try {
            classloader.loadClass("bla");
            fail();
        } catch(final ClassNotFoundException e) {
            log.info(e.getMessage());
        }
    }
    
    public void testDelegation() {
        classloader.clearAssertionStatus();
        classloader.setClassAssertionStatus("org.apache.commons.jci.ReloadingClassLoader",true);
        classloader.setDefaultAssertionStatus(false);
        classloader.setPackageAssertionStatus("org.apache.commons.jci", true);
        // FIXME: compare with delegation
    }
    
    protected void tearDown() throws Exception {
        fam.removeListener(listener);
        fam.stop();
        super.tearDown();
    }
    
}
