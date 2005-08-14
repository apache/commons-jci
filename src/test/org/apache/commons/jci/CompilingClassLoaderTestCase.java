package org.apache.commons.jci;

import java.io.File;
import org.apache.commons.jci.compilers.AbstractCompilerTestCase;
import org.apache.commons.jci.compilers.Programs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class CompilingClassLoaderTestCase extends AbstractCompilerTestCase {

    private final static Log log = LogFactory.getLog(CompilingClassLoaderTestCase.class);
    
    private final Signal reload = new Signal();

    private CompilingClassLoader cl;
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

        cl = new CompilingClassLoader(this.getClass().getClassLoader(), directory);
        cl.addListener(listener);
        cl.start();
    }

    private void initialCompile() throws Exception {
        delay();
        
        waitForSignal(reload);

        writeFile("jci/Simple.java",
                Programs.simple
        );
        
        writeFile("jci/Extended.java",
                Programs.extended
        );
        
        waitForSignal(reload);
    }
    
    
    public void testCompileProblems() throws Exception {
        delay();
        
        waitForSignal(reload);

        writeFile("jci/Simple.java",
                Programs.error
        );

        waitForSignal(reload);
        
        // FIXME
    }
    
    public void testCreate() throws Exception {
        initialCompile();
        
        Object o;
        
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(o.toString()));
        
        o = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(o.toString()));
    }

    public void testChange() throws Exception {        
        initialCompile();

        Object o;
        
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(o.toString()));
        
        o = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(o.toString()));

        writeFile("jci/Simple.java",
                Programs.SIMPLE
        );

        waitForSignal(reload);
    
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("SIMPLE".equals(o.toString()));
        
        o = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:SIMPLE".equals(o.toString()));
    }

    public void testDelete() throws Exception {
        initialCompile();

        Object o;
        
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(o.toString()));
        
        o = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(o.toString()));
        
        assertTrue(new File(directory, "jci/Extended.java").delete());
        
        waitForSignal(reload);

        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(o.toString()));

        try {
            o = cl.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertTrue("jci.Extended".equals(e.getMessage()));
        }
        
    }

    public void testDeleteDependency() throws Exception {        
        initialCompile();

        Object o;
        
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(o.toString()));
        
        o = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(o.toString()));
        
        assertTrue(new File(directory, "jci/Simple.java").delete());
        
        waitForSignal(reload);

        try {
            o = cl.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final NoClassDefFoundError e) {
            assertTrue("jci/Simple".equals(e.getMessage()));
        }
        
    }

    protected void tearDown() throws Exception {
        cl.removeListener(listener);
        cl.stop();
        super.tearDown();
    }
    
}
