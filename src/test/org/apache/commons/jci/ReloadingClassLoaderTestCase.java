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
package org.apache.commons.jci;

import java.io.File;
import org.apache.commons.jci.compilers.Programs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class ReloadingClassLoaderTestCase extends AbstractTestCase {

    private final static Log log = LogFactory.getLog(ReloadingClassLoaderTestCase.class);
    
    private final Signal reloadSignal = new Signal();
    private final Signal checkedSignal = new Signal();

    private ReloadingClassLoader cl;
    private ReloadingClassLoaderListener listener;

    private final byte[] clazzSimple;
    private final byte[] clazzSIMPLE;
    private final byte[] clazzExtended;
    
    public ReloadingClassLoaderTestCase() {
        clazzSimple = CompilerUtils.compile("jci.Simple", Programs.simple);
        clazzSIMPLE = CompilerUtils.compile("jci.Simple", Programs.SIMPLE);
        clazzExtended = CompilerUtils.compile(
                new String[] { "jci.Extended", "jci.Simple" },
                new String[] { Programs.extended, Programs.simple }
                );
        assertTrue(clazzSimple.length > 0);
        assertTrue(clazzSIMPLE.length > 0);
        assertTrue(clazzExtended.length > 0);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        listener = new ReloadingClassLoaderListener() {
            public void hasReloaded( final boolean pReload ) {                
                if (pReload) {
                    synchronized(reloadSignal) {
                        reloadSignal.triggered = true;
                        reloadSignal.notify();
                    }
                } else {
                    synchronized(checkedSignal) {
                        checkedSignal.triggered = true;
                        checkedSignal.notify();
                    }
                }
            }

        };

        cl = new ReloadingClassLoader(this.getClass().getClassLoader(), directory);
        cl.addListener(listener);
        cl.start();
    }

    public void testCreate() throws Exception {
        waitForSignal(checkedSignal);

        log.debug("creating class");
        
        delay();
        writeFile("jci/Simple.class", clazzSimple);
        waitForSignal(checkedSignal);
        
        final Object simple = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));        
    }

    public void testChange() throws Exception {        
        waitForSignal(checkedSignal);

        log.debug("creating class");

        delay();        
        writeFile("jci/Simple.class", clazzSimple);
        waitForSignal(checkedSignal);

        final Object simple = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        log.debug("changing class");
        
        delay();        
        writeFile("jci/Simple.class", clazzSIMPLE);
        waitForSignal(reloadSignal);
    
        final Object SIMPLE = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("SIMPLE".equals(SIMPLE.toString()));        
    }

    public void testDelete() throws Exception {
        waitForSignal(checkedSignal);

        log.debug("creating class");

        delay();        
        writeFile("jci/Simple.class", clazzSimple);
        waitForSignal(checkedSignal);

        final Object simple = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));

        log.debug("deleting class");
        
        assertTrue(new File(directory, "jci/Simple.class").delete());
        
        waitForSignal(reloadSignal);

        try {
            cl.loadClass("jci.Simple").newInstance();        
            fail();
        } catch(final ClassNotFoundException e) {
            assertTrue("jci.Simple".equals(e.getMessage()));
        }        
    }

    public void testDeleteDependency() throws Exception {        
        waitForSignal(checkedSignal);

        log.debug("creating classes");

        delay();        
        writeFile("jci/Simple.class", clazzSimple);
        writeFile("jci/Extended.class", clazzExtended);
        waitForSignal(checkedSignal);

        final Object simple = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));

        log.debug("deleting class dependency");
        
        assertTrue(new File(directory, "jci/Simple.class").delete());
        
        waitForSignal(reloadSignal);

        try {
            cl.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final NoClassDefFoundError e) {
            assertTrue("jci/Simple".equals(e.getMessage()));
        }
    }

    public void testClassNotFound() {
        try {
            cl.loadClass("bla");
            fail();
        } catch(final ClassNotFoundException e) {
            log.info(e.getMessage());
        }
    }
    
    protected void tearDown() throws Exception {
        cl.removeListener(listener);
        cl.stop();
        super.tearDown();
    }
    
}
