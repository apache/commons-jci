package org.apache.commons.jci;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class CompilingClassLoaderTestCase extends TestCase {

    private final static Log log = LogFactory.getLog(CompilingClassLoaderTestCase.class);
    
    private final Object signal = new Object();

    private CompilingClassLoader cl;
    private ReloadingListener listener;
    private File repository;
    
    private boolean reloaded;
    private void waitForReload() {
        log.debug("waiting for reload signal");
        int i = 0;
        while(true) {
            synchronized(signal) {
                if (!reloaded) {
                    try {
                        signal.wait(1000);
                    } catch (InterruptedException e) {
                        ;
                    }
                    if (++i > 7) {
                        fail("timeout");
                    }
                } else {
                    reloaded = false;
                    break;
                }
            }
        }
        
        log.debug("caught reload signal");
    }
    
    protected void setUp() throws Exception {
         repository = createTempDirectory();
        assertTrue(repository.exists());
        assertTrue(repository.isDirectory());

        listener = new ReloadingListener() {
            public void reload() {
                synchronized(signal) {
                    reloaded = true;
                    signal.notify();
                }
            }
        };

        cl = new CompilingClassLoader(this.getClass().getClassLoader(), repository);
        cl.addListener(listener);
        cl.start();
    }

    private void initialCompile() throws Exception {

        waitForReload();

        writeFile(new File(repository, "jci/Simple.java"),
                "package jci;\n"
                + "public class Simple { \n"
                + "  public String toString() { \n"
                + "    return \"Simple\"; \n"
                + "  } \n"
                + "} \n"
        );
        
        writeFile(new File(repository, "jci/Extended.java"),
                "package jci;\n"
                + "public class Extended extends Simple { \n"
                + "  public String toString() { \n"
                + "    return \"Extended:\" + super.toString(); \n"
                + "  } \n"
                + "} \n"
        );
        
        waitForReload();
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

        writeFile(new File(repository, "jci/Simple.java"),
                "package jci;\n"
                + "public class Simple { \n"
                + "  public String toString() { \n"
                + "    return \"SIMPLE\"; \n"
                + "  } \n"
                + "} \n"
        );

        waitForReload();
    
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
        
        assertTrue(new File(repository, "jci/Extended.java").delete());
        
        waitForReload();

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
        
        assertTrue(new File(repository, "jci/Simple.java").delete());
        
        waitForReload();

        try {
            o = cl.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final NoClassDefFoundError e) {
            assertTrue("jci/Simple".equals(e.getMessage()));
        }
        
    }
    
    protected void tearDown() throws Exception {
        cl.stop();
        FileUtils.deleteDirectory(repository);
    }
    
    
    private static void writeFile( final File pFile, final String pText ) throws IOException {
        final File parent = pFile.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("could not create" + parent);
            }
            log.debug("created directory " + parent.getAbsolutePath());
        }
        final FileWriter writer = new FileWriter(pFile);
        writer.write(pText);
        writer.close();
        
        assertTrue(pFile.exists());
    }
    
    private static File createTempDirectory() throws IOException {
        final File tempFile = File.createTempFile("jci", null);
        
        if (!tempFile.delete()) {
            throw new IOException();
        }
        
        if (!tempFile.mkdir()) {
            throw new IOException();
        }
        
        return tempFile;         
    }

}
