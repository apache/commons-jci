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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public final class FilesystemAlterationMonitorTestCase extends TestCase {

    private final Log log = LogFactory.getLog(FilesystemAlterationMonitorTestCase.class);

    private FilesystemAlterationMonitor fam;
    private MyFilesystemAlterationListener listener;

    protected File directory;

    
    protected void setUp() throws Exception {
        directory = createTempDirectory();
        assertTrue(directory.exists());
        assertTrue(directory.isDirectory());
    }
    
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(directory);
    }
    
    
    protected File createDirectory( final String pName ) throws Exception {
        final File newDirectory = new File(directory, pName);
        assertTrue(newDirectory.mkdir());
        assertTrue(newDirectory.exists());
        assertTrue(newDirectory.isDirectory());
        return newDirectory;
    }
    
    protected File writeFile( final String pName, final byte[] pData ) throws Exception {
        final File file = new File(directory, pName);
        final File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("could not create" + parent);
            }
        }
        
        log.debug("writing file " + pName + " (" + pData.length + " bytes)");
        
        final FileOutputStream os = new FileOutputStream(file);
        os.write(pData);
        os.close();
        
        assertTrue(file.exists());
        assertTrue(file.isFile());
        
        return file;
    }

    protected File writeFile( final String pName, final String pText ) throws Exception {
        final File file = new File(directory, pName);
        final File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("could not create" + parent);
            }
        }
        log.debug("writing " + file);
        final FileWriter writer = new FileWriter(file);
        writer.write(pText);
        writer.close();
        
        assertTrue(file.exists());
        assertTrue(file.isFile());
        
        return file;
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


    protected void delay() {
        try {
            Thread.sleep(1500);
        } catch (final InterruptedException e) {
        }
    }


    
    private static class MyFilesystemAlterationListener extends AbstractFilesystemAlterationListener {
    	
    	final File directory;
    	
    	public MyFilesystemAlterationListener( final File pDirectory ) {
    		directory = pDirectory;
    	}

		public File getRepository() {
			return directory;
		}

    }

    private void start() throws Exception {
        fam = new FilesystemAlterationMonitor();
        listener = new MyFilesystemAlterationListener(directory);
        fam.addListener(listener);
        fam.start();
        listener.waitForFirstCheck();
        delay(); // FIXME: really required?
    }
    
    private void stop() {
        fam.stop();
    }
    
    public void testListenerDoublication() throws Exception {
        fam = new FilesystemAlterationMonitor();
        listener = new MyFilesystemAlterationListener(directory);
        
        fam.addListener(listener);
        assertTrue(fam.getListeners().size() == 1);
        
        fam.addListener(listener); 
        assertTrue(fam.getListeners().size() == 1);
    }

    public void testDirectoryDoublication() throws Exception {
        fam = new FilesystemAlterationMonitor();

        fam.addListener(new MyFilesystemAlterationListener(directory)); 
        assertTrue(fam.getListenersFor(directory).size() == 1);
        
        fam.addListener(new MyFilesystemAlterationListener(directory)); 
        assertTrue(fam.getListenersFor(directory).size() == 2);
    }

    public void testCreateFileDetection() throws Exception {
        start();
        
        writeFile("file", "file");
        
        listener.waitForEvent();
        
        assertTrue(listener.getCreatedFiles() == 1);
        
        stop();
    }

    public void testCreateDirectoryDetection() throws Exception {
        start();

        createDirectory("dir");
        
        listener.waitForEvent();
        
        assertTrue(listener.getCreatedDirectories() == 1);
        
        stop();
    }

    public void testDeleteFileDetection() throws Exception {
        start();

        final File file = writeFile("file", "file");
        
        listener.waitForEvent();
        
        assertTrue(listener.getCreatedFiles() == 1);
        
        file.delete();
        assertTrue(!file.exists());

        listener.waitForEvent();
        
        assertTrue(listener.getDeletedFiles() == 1);
        
        stop();        
    }

    public void testDeleteDirectoryDetection() throws Exception {
        start();

        final File dir = createDirectory("dir");
        createDirectory("dir/sub");
        
        listener.waitForEvent();
        
        assertTrue(listener.getCreatedDirectories() == 2);

        delay();
        
        FileUtils.deleteDirectory(dir);
        assertTrue(!dir.exists());

        listener.waitForEvent();
        
        assertTrue(listener.getDeletedDirectories() == 2);

        stop();
    }

    public void testModifyFileDetection() throws Exception {
        start();

        writeFile("file", "file");
        
        listener.waitForEvent();
        
        assertTrue(listener.getCreatedFiles() == 1);

        delay();

        writeFile("file", "changed file");

        listener.waitForEvent();
        
        assertTrue(listener.getChangedFiles() == 1);
        
        stop();
    }

    public void testCreatingLocalDirectoryChangesLastModified() throws Exception {
        final long modified = directory.lastModified();

        delay();
        
        createDirectory("directory");

        delay();
               
        assertTrue(directory.lastModified() != modified);
    }

    public void testCreatingLocalFileChangesLastModified() throws Exception {
        final long modified = directory.lastModified();

        delay();
        
        writeFile("file", "file");

        delay();

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
    	
    	final long interval = 100;
    	
        start();
        fam.setInterval(interval);

        listener.waitForCheck();
        long t1 = System.currentTimeMillis();

        listener.waitForCheck();
        long t2 = System.currentTimeMillis();
        
        long diff = t2-t1;
        
        // interval should be at around the same interval
        assertTrue( (diff > (interval-20)) && (diff < (interval+20)) );
        
        stop();
    }    
}
