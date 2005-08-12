package org.apache.commons.jci.monitor;

import java.io.File;
import org.apache.commons.jci.AbstractTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public final class FilesystemAlterationMonitorTestCase extends AbstractTestCase {

    private final static Log log = LogFactory.getLog(FilesystemAlterationMonitorTestCase.class);

    private final Signal signal = new Signal();

    private FilesystemAlterationMonitor fam;
    private MyFilesystemAlterationListener listener;
    private Thread thread; 

    private class MyFilesystemAlterationListener implements FilesystemAlterationListener {
        private int started;
        private int stopped;
        private int createdFiles;
        private int changedFiles;
        private int deletedFiles;
        private int createdDirs;
        private int changedDirs;
        private int deletedDirs;
 
        public int getChangedDirs() {
            return changedDirs;
        }
        public int getChangedFiles() {
            return changedFiles;
        }
        public int getCreatedDirs() {
            return createdDirs;
        }
        public int getCreatedFiles() {
            return createdFiles;
        }
        public int getDeletedDirs() {
            return deletedDirs;
        }
        public int getDeletedFiles() {
            return deletedFiles;
        }
        public int getStarted() {
            return started;
        }
        public int getStopped() {
            return stopped;
        }
                 
        public void onStart(final File repository) {
            ++started;
        }
        public void onStop(final File repository) {
            ++stopped;
            synchronized(signal) {
                signal.triggered = true;
                signal.notify();
            }
        }
        public void onCreateFile( final File file ) {
            ++createdFiles;
        }
        public void onChangeFile( final File file ) {                
            ++changedFiles;
        }
        public void onDeleteFile( final File file ) {
            ++deletedFiles;
        }
        public void onCreateDirectory( final File file ) {                
            ++createdDirs;
        }
        public void onChangeDirectory( final File file ) {                
            ++changedDirs;
        }
        public void onDeleteDirectory( final File file ) {
            ++deletedDirs;
        }       
    }

    private void start() throws Exception {
        fam = new FilesystemAlterationMonitor();
        listener = new MyFilesystemAlterationListener();        
        fam.addListener(listener, directory);
        thread = new Thread(fam); 
        thread.start();

        waitForSignal(signal);
    }
    
    private void stop() throws Exception {
        fam.stop();
        thread.join();        
    }
    
    public void testCreateFileDetection() throws Exception {
        start();
        
        delay();
        
        writeFile("file", "file");
        
        waitForSignal(signal);
        
        assertTrue(listener.createdFiles == 1);
        
        stop();
    }

    public void testCreateDirectoryDetection() throws Exception {
        start();

        delay();

        createDirectory("dir");
        
        waitForSignal(signal);
        
        assertTrue(listener.createdDirs == 1);
        
        stop();
    }

    public void testDeleteFileDetection() throws Exception {
        start();

        delay();
        
        final File file = writeFile("file", "file");
        
        waitForSignal(signal);
        
        assertTrue(listener.createdFiles == 1);
        
        file.delete();
        assertTrue(!file.exists());

        waitForSignal(signal);
        
        assertTrue(listener.deletedFiles == 1);
        
        stop();        
    }

    public void testDeleteDirectoryDetection() throws Exception {
        start();

        delay();
        
        final File dir = createDirectory("dir");
        
        waitForSignal(signal);
        
        assertTrue(listener.createdDirs == 1);

        waitForSignal(signal);

        delay();
        
        dir.delete();
        assertTrue(!dir.exists());

        waitForSignal(signal);
        
        assertTrue(listener.deletedDirs == 1);

        stop();
    }

    public void testModifyFileDetection() throws Exception {
        start();

        delay();
        
        writeFile("file", "file");
        
        waitForSignal(signal);
        
        assertTrue(listener.createdFiles == 1);

        waitForSignal(signal);

        writeFile("file", "changed file");

        waitForSignal(signal);
        
        assertTrue(listener.changedFiles == 1);
        
        stop();
    }

    public void testCreatingLocalDirectoryChangesLastModified() throws Exception {
        final long modified = directory.lastModified();

        delay();
        
        createDirectory("directory");
               
        assertTrue(directory.lastModified() != modified);
    }

    public void testCreatingLocalFileChangesLastModified() throws Exception {
        final long modified = directory.lastModified();

        delay();

        writeFile("file", "file");

        assertTrue(directory.lastModified() != modified);
    }

    public void testCreatingSubDirectoryChangesLastModified() throws Exception {
        createDirectory("dir");

        final long modified = directory.lastModified();

        delay();

        createDirectory("dir/sub");

        assertTrue(directory.lastModified() == modified);
    }

    public void testCreatingFileInSubDirectoryChangesLastModified() throws Exception {
        createDirectory("dir");

        final long modified = directory.lastModified();

        delay();
                
        writeFile("dir/file", "file");

        assertTrue(directory.lastModified() == modified);
    }
    
}
