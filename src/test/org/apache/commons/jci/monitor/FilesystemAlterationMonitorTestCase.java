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
        
        log.debug("creating file");
        
        final File file = new File(directory, "file");
        writeFile(file, "file");
        assertTrue(file.exists());
        assertTrue(file.isFile());
        
        waitForSignal(signal);
        
        assertTrue(listener.createdFiles == 1);
        
        stop();
    }

    public void testCreateDirectoryDetection() throws Exception {
        start();
        
        log.debug("creating dir");

        final File newDirectory = new File(directory, "directory");
        assertTrue(newDirectory.mkdir());
        assertTrue(newDirectory.exists());
        assertTrue(newDirectory.isDirectory());
        
        waitForSignal(signal);
        
        assertTrue(listener.createdDirs == 1);
        
        stop();
    }

    public void testDeleteFileDetection() throws Exception {
        start();
        
        log.debug("creating file");
        
        final File file = new File(directory, "file");
        writeFile(file, "file");
        assertTrue(file.exists());
        assertTrue(file.isFile());
        
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
        
        log.debug("creating dir");

        final File newDirectory = new File(directory, "directory");
        assertTrue(newDirectory.mkdir());
        assertTrue(newDirectory.exists());
        assertTrue(newDirectory.isDirectory());
        
        waitForSignal(signal);
        
        assertTrue(listener.createdDirs == 1);

        waitForSignal(signal);

        newDirectory.delete();
        assertTrue(!newDirectory.exists());

        waitForSignal(signal);
        
        assertTrue(listener.deletedDirs == 1);

        stop();
    }

    public void testModifyFileDetection() throws Exception {
        start();
        
        log.debug("creating file");
        
        final File file = new File(directory, "file");
        writeFile(file, "file");
        assertTrue(file.exists());
        assertTrue(file.isFile());
        
        waitForSignal(signal);
        
        assertTrue(listener.createdFiles == 1);

        waitForSignal(signal);

        writeFile(file, "changed file");

        waitForSignal(signal);
        
        assertTrue(listener.changedFiles == 1);
        
        stop();
    }

    public void testCreatingLocalDirectoryChangesLastModified() throws Exception {
        final long modified = directory.lastModified();
        
        final File newDirectory = new File(directory, "directory");
        assertTrue(newDirectory.mkdir());
        assertTrue(newDirectory.exists());
        assertTrue(newDirectory.isDirectory());
        
        while(directory.lastModified() == modified) {
            Thread.sleep(100);
            System.out.print('.');
        }
        
        
        assertTrue(directory.lastModified() != modified);
    }

    public void testCreatingLocalFileChangesLastModified() throws Exception {
        final long modified = directory.lastModified();
        
        final File file = new File(directory, "file");
        writeFile(file, "file");
        assertTrue(file.exists());
        assertTrue(file.isFile());

        assertTrue(directory.lastModified() != modified);
    }

    public void testCreatingSubDirectoryChangesLastModified() throws Exception {
        final File newDirectory = new File(directory, "directory");
        assertTrue(newDirectory.mkdir());
        assertTrue(newDirectory.exists());
        assertTrue(newDirectory.isDirectory());

        final long modified = directory.lastModified();
        
        final File newSubDirectory = new File(newDirectory, "sub");
        assertTrue(newSubDirectory.mkdir());
        assertTrue(newSubDirectory.exists());
        assertTrue(newSubDirectory.isDirectory());

        assertTrue(directory.lastModified() != modified);
    }

    public void testCreatingFileInSubDirectoryChangesLastModified() throws Exception {
        final File newDirectory = new File(directory, "directory");
        assertTrue(newDirectory.mkdir());
        assertTrue(newDirectory.exists());
        assertTrue(newDirectory.isDirectory());

        final long modified = directory.lastModified();
        
        final File file = new File(newDirectory, "file");
        writeFile(file, "file");
        assertTrue(file.exists());
        assertTrue(file.isFile());

        assertTrue(directory.lastModified() != modified);
    }
    
}
