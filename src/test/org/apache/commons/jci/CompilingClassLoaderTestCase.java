package org.apache.commons.jci;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jci.compilers.AbstractCompilerTestCase;
import org.apache.commons.jci.compilers.JavaSources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class CompilingClassLoaderTestCase extends AbstractCompilerTestCase {

    private final static Log log = LogFactory.getLog(CompilingClassLoaderTestCase.class);
    
    private final Signal reloadSignal = new Signal();

    private CompilingClassLoader cl;
    private ReloadingClassLoaderListener listener;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        listener = new ReloadingClassLoaderListener() {
            public void hasChecked() {
            }
            public void hasReloaded(boolean pReload) {
                synchronized(reloadSignal) {
                    reloadSignal.triggered = true;
                    reloadSignal.notify();
                }
            }
        };

        cl = new CompilingClassLoader(this.getClass().getClassLoader(), directory);
        cl.addListener(listener);
        cl.start();
    }

    private void initialCompile() throws Exception {
        delay();        
        writeFile("jci/Simple.java", JavaSources.simple);        
        writeFile("jci/Extended.java", JavaSources.extended);        
        waitForSignal(reloadSignal);
    }
    
    
    public void testCompileProblems() throws Exception {
        delay();        
        writeFile("jci/Simple.java", JavaSources.error);
        waitForSignal(reloadSignal);
        
        // FIXME
    }
    
    public void testCreate() throws Exception {
        initialCompile();
        
        final Object simple = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));
    }

    public void testChange() throws Exception {        
        initialCompile();

        final Object simple = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));

        delay();
        writeFile("jci/Simple.java", JavaSources.SIMPLE);
        waitForSignal(reloadSignal);
    
        final Object SIMPLE = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("SIMPLE".equals(SIMPLE.toString()));
        
        final Object newExtended = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:SIMPLE".equals(newExtended.toString()));
    }

    public void testDelete() throws Exception {
        initialCompile();

        final Object simple = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));
        
        delay();
        assertTrue(new File(directory, "jci/Extended.java").delete());
        waitForSignal(reloadSignal);

        final Object oldSimple = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(oldSimple.toString()));

        try {
            cl.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertTrue("jci.Extended".equals(e.getMessage()));
        }
        
        delay();
        FileUtils.deleteDirectory(new File(directory, "jci"));
        waitForSignal(reloadSignal);

        try {
            cl.loadClass("jci.Simple").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertTrue("jci.Simple".equals(e.getMessage()));
        }

    }

    public void testDeleteDependency() throws Exception {        
        initialCompile();

        final Object simple = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));
        
        delay();
        assertTrue(new File(directory, "jci/Simple.java").delete());
        waitForSignal(reloadSignal);

        try {
            cl.loadClass("jci.Extended").newInstance();
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
