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
    
    private final Signal reload = new Signal();

    private ReloadingClassLoader cl;
    private ReloadingClassLoaderListener listener;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        listener = new ReloadingClassLoaderListener() {
            public void reload() {
                synchronized(reload) {
                    reload.triggered = true;
                    reload.notify();
                }
            }
        };

        cl = new ReloadingClassLoader(this.getClass().getClassLoader(), directory);
        cl.addListener(listener);
        cl.start();
    }

    private void initialCompile() throws Exception {
        delay();
        
        // writeFile
        // compile file
        
        waitForSignal(reload);

        writeFile("jci/Simple.class",
                Programs.simple
        );
        
        waitForSignal(reload);
    }
    
    
    public void testCreate() throws Exception {
        initialCompile();
        
        Object o;
        
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(o.toString()));        
    }

    public void testChange() throws Exception {        
        initialCompile();

        Object o;
        
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(o.toString()));
        
        writeFile("jci/Simple.java",
                Programs.SIMPLE
        );

        waitForSignal(reload);
    
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("SIMPLE".equals(o.toString()));        
    }

    public void testDelete() throws Exception {
        initialCompile();

        Object o;
        
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(o.toString()));
        
        assertTrue(new File(directory, "jci/Simple.java").delete());
        
        waitForSignal(reload);

        try {
            o = cl.loadClass("jci.Simple").newInstance();        
            fail();
        } catch(final ClassNotFoundException e) {
        }
        
    }

    public void testDeleteDependency() throws Exception {        
        initialCompile();

        Object o;
        
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(o.toString()));
        
        o = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(o.toString()));
        
        assertTrue(new File(directory, "jci/Simple.class").delete());
        
        waitForSignal(reload);

        try {
            o = cl.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final NoClassDefFoundError e) {
            assertTrue("jci/Simple".equals(e.getMessage()));
        }
        
    }

    public void testClassNotFound() {
        try {
            Object o = cl.loadClass("bla");
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
