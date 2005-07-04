package org.apache.commons.jci.monitor;

import java.io.File;
import junit.framework.TestCase;



public final class FilesystemAlterationMonitorTestCase extends TestCase {

    class MyFilesystemAlterationListener implements FilesystemAlterationListener {
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
                 
        public void onStart() {
            ++started;
        }
        public void onStop() {
            ++stopped;
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
    
    public void testCreateDetection() {
        final File repository = new File("");
        final FilesystemAlterationMonitor fam = new FilesystemAlterationMonitor(); 
        final MyFilesystemAlterationListener listener = new MyFilesystemAlterationListener();
        
        fam.addListener(listener, repository);
        
        Thread myThread = new Thread(fam); 
        myThread.start();
        
        fam.stop();
    }

    public void testDeleteDetection() {
        
    }

    public void testModifyDetection() {
        
    }

}
