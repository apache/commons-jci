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
package org.apache.commons.jci.monitor;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jci.AbstractTestCase;
import org.apache.commons.jci.listeners.AbstractListener;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public final class FilesystemAlterationMonitorTestCase extends AbstractTestCase {

    private final static Log log = LogFactory.getLog(FilesystemAlterationMonitorTestCase.class);

    private final Signal signal = new Signal();

    private FilesystemAlterationMonitor fam;
    private MyFilesystemAlterationListener listener;

    private class MyFilesystemAlterationListener extends AbstractListener {
        private int started;
        private int stopped;
        private int createdFiles;
        private int changedFiles;
        private int deletedFiles;
        private int createdDirs;
        private int changedDirs;
        private int deletedDirs;
 
        public MyFilesystemAlterationListener(final File pRepository) {
            super(pRepository);
        }
        
        public ResourceStore getStore() {
            return null;
        }

        protected void needsReload( boolean pReload ) {
            // prevent NPE
        }

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

    private void start() {
        fam = new FilesystemAlterationMonitor();
        listener = new MyFilesystemAlterationListener(directory);
        fam.addListener(listener);
        fam.start();
        waitForSignal(signal);
    }
    
    private void stop() {
        fam.stop();
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
        createDirectory("dir/sub");
        
        waitForSignal(signal);
        
        assertTrue(listener.createdDirs == 2);

        waitForSignal(signal);

        delay();
        
        FileUtils.deleteDirectory(dir);
        assertTrue(!dir.exists());

        waitForSignal(signal);
        
        assertTrue(listener.deletedDirs == 2);

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
    
    public void testInterval() throws Exception {
        start();
        fam.setInterval(1000);
        stop();
    }
    
    public void testListener() throws Exception {
        start();
        fam.removeListener(listener);
        stop();
    }
    
}
