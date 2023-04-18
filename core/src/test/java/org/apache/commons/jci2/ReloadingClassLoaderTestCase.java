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

package org.apache.commons.jci2;

import java.io.File;

import org.apache.commons.jci2.classes.ExtendedDump;
import org.apache.commons.jci2.classes.SimpleDump;
import org.apache.commons.jci2.listeners.ReloadingListener;
import org.apache.commons.jci2.monitor.FilesystemAlterationMonitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author tcurdt
 */
public final class ReloadingClassLoaderTestCase extends AbstractTestCase {

    private final Log log = LogFactory.getLog(ReloadingClassLoaderTestCase.class);

    private ReloadingClassLoader classloader;
    private ReloadingListener listener;
    private FilesystemAlterationMonitor fam;

    private final byte[] clazzSimple1;
    private final byte[] clazzSimple2;
    private final byte[] clazzExtended;

    public ReloadingClassLoaderTestCase() throws Exception {
        clazzSimple1 = SimpleDump.dump("Simple1");
        clazzSimple2 = SimpleDump.dump("Simple2");
        clazzExtended = ExtendedDump.dump();
        assertTrue(clazzSimple1.length > 0);
        assertTrue(clazzSimple2.length > 0);
        assertTrue(clazzExtended.length > 0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        classloader = new ReloadingClassLoader(this.getClass().getClassLoader());
        listener = new ReloadingListener();

        listener.addReloadNotificationListener(classloader);

        fam = new FilesystemAlterationMonitor();
        fam.addListener(directory, listener);
        fam.start();
    }

    public void testCreate() throws Exception {
        listener.waitForFirstCheck();

        log.debug("creating class");
        writeFile("jci2/Simple.class", clazzSimple1);
        listener.waitForCheck();

        final Object simple = classloader.loadClass("jci2.Simple").newInstance();
        assertEquals("Simple1", simple.toString());
    }

    public void testChange() throws Exception {
        listener.waitForFirstCheck();

        log.debug("creating class");
        writeFile("jci2/Simple.class", clazzSimple1);
        listener.waitForCheck();

        final Object simple1 = classloader.loadClass("jci2.Simple").newInstance();
        assertEquals("Simple1", simple1.toString());

        log.debug("changing class");
        writeFile("jci2/Simple.class", clazzSimple2);
        listener.waitForEvent();

        final Object simple2 = classloader.loadClass("jci2.Simple").newInstance();
        assertEquals("Simple2", simple2.toString());
    }

    public void testDelete() throws Exception {
        listener.waitForFirstCheck();

        log.debug("creating class");
        writeFile("jci2/Simple.class", clazzSimple1);
        listener.waitForCheck();

        final Object simple = classloader.loadClass("jci2.Simple").newInstance();
        assertEquals("Simple1", simple.toString());

        log.debug("deleting class");
        assertTrue(new File(directory, "jci2/Simple.class").delete());
        listener.waitForEvent();

        try {
            classloader.loadClass("jci2.Simple").newInstance();
            fail();
        } catch (final ClassNotFoundException e) {
            assertEquals("jci2.Simple", e.getMessage());
        }
    }

    public void testDeleteDependency() throws Exception {
        listener.waitForFirstCheck();

        log.debug("creating classes");
        writeFile("jci2/Simple.class", clazzSimple1);
        writeFile("jci2/Extended.class", clazzExtended);
        listener.waitForCheck();

        final Object simple = classloader.loadClass("jci2.Simple").newInstance();
        assertEquals("Simple1", simple.toString());

        final Object extended = classloader.loadClass("jci2.Extended").newInstance();
        assertEquals("Extended:Simple1", extended.toString());

        log.debug("deleting class dependency");
        assertTrue(new File(directory, "jci2/Simple.class").delete());
        listener.waitForEvent();

        try {
            classloader.loadClass("jci2.Extended").newInstance();
            fail();
        } catch (final NoClassDefFoundError e) {
            assertEquals("jci2/Simple", e.getMessage());
        }
    }

    public void testClassNotFound() {
        try {
            classloader.loadClass("bla");
            fail();
        } catch (final ClassNotFoundException e) {
        }
    }

    public void testDelegation() {
        classloader.clearAssertionStatus();
        classloader.setClassAssertionStatus("org.apache.commons.jci2.ReloadingClassLoader", true);
        classloader.setDefaultAssertionStatus(false);
        classloader.setPackageAssertionStatus("org.apache.commons.jci2", true);
        // FIXME: compare with delegation
    }

    @Override
    protected void tearDown() throws Exception {
        fam.removeListener(listener);
        fam.stop();
        super.tearDown();
    }

}
