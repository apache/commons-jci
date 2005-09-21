package org.apache.commons.jci;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jci.compilers.AbstractCompilerTestCase;
import org.apache.commons.jci.compilers.JavaSources;
import org.apache.commons.jci.listeners.CompilingListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class CompilingClassLoaderTestCase extends AbstractCompilerTestCase {

    private final static Log log = LogFactory.getLog(CompilingClassLoaderTestCase.class);
    
    private final Signal reloadSignal = new Signal();

    private ReloadingClassLoader classloader;
    private CompilingListener listener;
    private FilesystemAlterationMonitor fam;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        classloader = new ReloadingClassLoader(this.getClass().getClassLoader());
        listener = new CompilingListener(directory) {
            protected void needsReload(final boolean pReload) {
                super.needsReload(pReload);
                if (pReload) {
                    synchronized(reloadSignal) {
                        reloadSignal.triggered = true;
                        reloadSignal.notify();
                    }
                }
            }  
        };        
        classloader.addListener(listener);
        
        fam = new FilesystemAlterationMonitor();
        fam.addListener(listener);
        fam.start();
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
        
        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));
    }

    public void testChange() throws Exception {        
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));

        delay();
        writeFile("jci/Simple.java", JavaSources.SIMPLE);
        waitForSignal(reloadSignal);
    
        final Object SIMPLE = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("SIMPLE".equals(SIMPLE.toString()));
        
        final Object newExtended = classloader.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:SIMPLE".equals(newExtended.toString()));
    }

    public void testDelete() throws Exception {
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));
        
        delay();
        assertTrue(new File(directory, "jci/Extended.java").delete());
        waitForSignal(reloadSignal);

        final Object oldSimple = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(oldSimple.toString()));

        try {
            classloader.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertTrue("jci.Extended".equals(e.getMessage()));
        }
        
        delay();
        FileUtils.deleteDirectory(new File(directory, "jci"));
        waitForSignal(reloadSignal);

        try {
            classloader.loadClass("jci.Simple").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertTrue("jci.Simple".equals(e.getMessage()));
        }

    }

    public void testDeleteDependency() throws Exception {        
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));
        
        delay();
        assertTrue(new File(directory, "jci/Simple.java").delete());
        waitForSignal(reloadSignal);

        try {
            classloader.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final NoClassDefFoundError e) {
            assertTrue("jci/Simple".equals(e.getMessage()));
        }
        
    }

    protected void tearDown() throws Exception {
        fam.removeListener(listener);
        fam.stop();
        super.tearDown();
    }
    
}
