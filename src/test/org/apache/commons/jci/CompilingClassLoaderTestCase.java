package org.apache.commons.jci;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import junit.framework.TestCase;


public final class CompilingClassLoaderTestCase extends TestCase {

    private final static Log log = LogFactory.getLog(CompilingClassLoaderTestCase.class);
    
    private CompilingClassLoader cl;
    private ReloadingListener listener;
    private File repository;
    private boolean reloaded; //FIXME
    
    protected void setUp() throws Exception {
        
        repository = createTempDirectory();
        assertTrue(repository.exists());
        assertTrue(repository.isDirectory());

        listener = new ReloadingListener() {
            public void reload() {
                log.debug("notifying about the reload");
                reloaded = true; //FIXME
            }
        };

        cl = new CompilingClassLoader(this.getClass().getClassLoader(), repository);
        cl.addListener(listener);
        cl.start();
        
        // FIXME
        log.debug("waiting for reload");
        while(true) {
            if (reloaded) break;
            Thread.sleep(200);
        }
        log.debug("reloaded");

        reloaded = false;
    }
    
    public void testCreateCompilation() throws Exception {

        log.debug("creating java files");
        
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
        
        // FIXME
        log.debug("waiting for reload");
        while(true) {
            if (reloaded) break;
            Thread.sleep(200);
        }
        log.debug("reloaded");
        
        Object o;
        
        o = cl.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(o.toString()));
        
        o = cl.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(o.toString()));
    }

    public void testChangeCompilation() {        
    }

    public void testDeleteCompilation() {        
    }

    
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(repository);
    }
    
    
    private static void writeFile( final File pFile, final String pText ) throws IOException {
        final File parent = pFile.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("could not create" + parent);
            }
        }
        final FileWriter writer = new FileWriter(pFile);
        writer.write(pText);
        writer.close();
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
