/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.commons.jci.listeners.NotifyingListener;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public final class FilesystemAlterationMonitorTestCase extends AbstractTestCase {

    private final static Log log = LogFactory.getLog(FilesystemAlterationMonitorTestCase.class);

    private FilesystemAlterationMonitor fam;
    private MyFilesystemAlterationListener listener;

    private class MyFilesystemAlterationListener extends NotifyingListener {
        private int started;
        private int stopped;
        private int createdFiles;
        private int changedFiles;
        private int deletedFiles;
        private int createdDirs;
        private int changedDirs;
        private int deletedDirs;
        private boolean changed;
 
        public MyFilesystemAlterationListener(final File pRepository) {
            super(pRepository);
        }
        
        public ResourceStore getStore() {
            return null;
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
            changed = false;
            ++started;
            log.debug("onStart");
        }
        public void onStop() {
            ++stopped;
            log.debug("onStop");
            
            checked(changed);
        }
        public void onCreateFile( final File file ) {
            ++createdFiles;
            changed = true;
            log.debug("onCreateFile " + file);
        }
        public void onChangeFile( final File file ) {                
            ++changedFiles;
            changed = true;
            log.debug("onChangeFile " + file);
        }
        public void onDeleteFile( final File file ) {
            ++deletedFiles;
            changed = true;
            log.debug("onDeleteFile " + file);
        }
        public void onCreateDirectory( final File file ) {                
            ++createdDirs;
            changed = true;
            log.debug("onCreateDirectory " + file);
        }
        public void onChangeDirectory( final File file ) {                
            ++changedDirs;
            changed = true;
            log.debug("onChangeDirectory " + file);
        }
        public void onDeleteDirectory( final File file ) {
            ++deletedDirs;
            changed = true;
            log.debug("onDeleteDirectory " + file);
        }
    }

    private void start() throws Exception {
        fam = new FilesystemAlterationMonitor();
        listener = new MyFilesystemAlterationListener(directory);
        fam.addListener(listener);
        fam.start();
        listener.waitForFirstCheck();
    }
    
    private void stop() {
        fam.stop();
    }
    
    public void testListenerDoublication() throws Exception {
        fam = new FilesystemAlterationMonitor();
        listener = new MyFilesystemAlterationListener(directory);
        
        fam.addListener(listener);
        log.debug(fam);
        assertTrue(fam.getListeners().size() == 1);
        
        fam.addListener(listener); 
        log.debug(fam);        
        assertTrue(fam.getListeners().size() == 1);
    }

    public void testDirectoryDoublication() throws Exception {
        fam = new FilesystemAlterationMonitor();

        fam.addListener(new MyFilesystemAlterationListener(directory)); 
        log.debug(fam);
        assertTrue(fam.getListenersFor(directory).size() == 1);
        
        fam.addListener(new MyFilesystemAlterationListener(directory)); 
        log.debug(fam);        
        assertTrue(fam.getListenersFor(directory).size() == 2);
    }

    public void testListener() throws Exception {
        start();
        log.debug(fam);
        fam.removeListener(listener);
        log.debug(fam);
        stop();
    }

    public void testCreateFileDetection() throws Exception {
        start();
        
        delay();
        
        writeFile("file", "file");
        
        listener.waitForNotification();
        
        assertTrue(listener.createdFiles == 1);
        
        stop();
    }

    public void testCreateDirectoryDetection() throws Exception {
        start();

        delay();

        createDirectory("dir");
        
        listener.waitForNotification();
        
        assertTrue(listener.createdDirs == 1);
        
        stop();
    }

    public void testDeleteFileDetection() throws Exception {
        start();

        delay();
        
        final File file = writeFile("file", "file");
        
        listener.waitForNotification();
        
        assertTrue(listener.createdFiles == 1);
        
        file.delete();
        assertTrue(!file.exists());

        listener.waitForNotification();
        
        assertTrue(listener.deletedFiles == 1);
        
        stop();        
    }

    public void testDeleteDirectoryDetection() throws Exception {
        start();

        delay();
        
        final File dir = createDirectory("dir");
        createDirectory("dir/sub");
        
        listener.waitForNotification();
        
        assertTrue(listener.createdDirs == 2);

        delay();
        
        FileUtils.deleteDirectory(dir);
        assertTrue(!dir.exists());

        listener.waitForNotification();
        
        assertTrue(listener.deletedDirs == 2);

        stop();
    }

    public void testModifyFileDetection() throws Exception {
        start();

        delay();
        
        writeFile("file", "file");
        
        listener.waitForNotification();
        
        assertTrue(listener.createdFiles == 1);

        delay();

        writeFile("file", "changed file");

        listener.waitForNotification();
        
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
        fam.setInterval(100);
        stop();
    }    
}
