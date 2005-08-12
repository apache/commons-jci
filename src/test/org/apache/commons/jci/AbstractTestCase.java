package org.apache.commons.jci;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class AbstractTestCase extends TestCase {

    private final static Log log = LogFactory.getLog(AbstractTestCase.class);

    protected File directory;

    public final class Signal {
        public boolean triggered;
    }

    /*
    public void runBare() throws Throwable {
        try {
            setUp();
            runTest();
        } finally {
            tearDown();
        }
    }
    */
    
    protected void waitForSignal(final Signal pSignal) {
        log.debug("waiting for signal");
        int i = 0;
        while(true) {
            synchronized(pSignal) {
                if (!pSignal.triggered) {
                    try {
                        pSignal.wait(1000);
                    } catch (InterruptedException e) {
                        ;
                    }
                    if (++i > 7) {
                        fail("timeout");
                    }
                } else {
                    pSignal.triggered = false;
                    break;
                }
            }
        }
        
        log.debug("caught signal");
    }
    

    
    protected void setUp() throws Exception {
        directory = createTempDirectory();
        assertTrue(directory.exists());
        assertTrue(directory.isDirectory());
    }
    
    
    protected File createDirectory( final String pName ) throws Exception {
        final File newDirectory = new File(directory, pName);
        assertTrue(newDirectory.mkdir());
        assertTrue(newDirectory.exists());
        assertTrue(newDirectory.isDirectory());
        return newDirectory;
    }
    
    protected File writeFile( final String pName, final String pText ) throws Exception {
        final File file = new File(directory, pName);
        final File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("could not create" + parent);
            }
        }
        final FileWriter writer = new FileWriter(file);
        writer.write(pText);
        writer.close();
        
        assertTrue(file.exists());
        assertTrue(file.isFile());
        
        return file;
    }
    
    protected void delay() {
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e) {
            ;
        }
    }
    
    protected File createTempDirectory() throws IOException {
        final File tempFile = File.createTempFile("jci", null);
        
        if (!tempFile.delete()) {
            throw new IOException();
        }
        
        if (!tempFile.mkdir()) {
            throw new IOException();
        }
        
        return tempFile;         
    }


    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(directory);
    }


}
