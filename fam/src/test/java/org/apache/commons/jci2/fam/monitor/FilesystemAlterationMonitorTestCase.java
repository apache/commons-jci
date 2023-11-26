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

package org.apache.commons.jci2.fam.monitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.jci2.fam.listeners.AbstractFilesystemAlterationListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 *
 * @author tcurdt
 */
public final class FilesystemAlterationMonitorTestCase extends TestCase {

    private final Log log = LogFactory.getLog(FilesystemAlterationMonitorTestCase.class);

    private FilesystemAlterationMonitor fam;
    private MyFilesystemAlterationListener listener;

    private File directory;

    @Override
    protected void setUp() throws Exception {
        directory = createTempDirectory();
        assertTrue(directory.exists());
        assertTrue(directory.isDirectory());
    }

    @Override
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
        if (!parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("could not create" + parent);
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
        if (!parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("could not create" + parent);
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
        final File tempFile = File.createTempFile("jci2", null);

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

    private static final class MyFilesystemAlterationListener extends AbstractFilesystemAlterationListener {
    }

    private void start() throws Exception {
        fam = new FilesystemAlterationMonitor();
        listener = new MyFilesystemAlterationListener();
        fam.addListener(directory, listener);
        fam.start();
        listener.waitForFirstCheck();
    }

    private void stop() {
        fam.stop();
    }

    public void testListenerDoublication() throws Exception {
        fam = new FilesystemAlterationMonitor();
        listener = new MyFilesystemAlterationListener();

        fam.addListener(directory, listener);
        assertEquals(1, fam.getListenersFor(directory).length);

        fam.addListener(directory, listener);
        assertEquals(1, fam.getListenersFor(directory).length);

        fam.removeListener(listener);
        assertEquals(0, fam.getListenersFor(directory).length);
}

    public void testDirectoryDoublication() throws Exception {
        fam = new FilesystemAlterationMonitor();

        fam.addListener(directory, new MyFilesystemAlterationListener());
        assertEquals(1, fam.getListenersFor(directory).length);

        fam.addListener(directory, new MyFilesystemAlterationListener());
        assertEquals(2, fam.getListenersFor(directory).length);
    }

    public void testCreateFileDetection() throws Exception {
        start();

        writeFile("file", "file");

        listener.waitForCheck();

        assertEquals(1, listener.getCreatedFiles().size());

        stop();
    }

    public void testTimeout() throws Exception {
    	listener = new MyFilesystemAlterationListener();

    	try {
        	listener.waitForFirstCheck();
        	fail("should be an timeout");
        } catch (final Exception e) {
        	assertEquals("timeout", e.getMessage());
        }

        start();

        try {
        	listener.waitForEvent();
        	fail("should be an timeout");
        } catch (final Exception e) {
        	assertEquals("timeout", e.getMessage());
        }

        stop();

        try {
        	listener.waitForCheck();
        	fail("should be an timeout");
        } catch (final Exception e) {
        	assertEquals("timeout", e.getMessage());
        }

    }

    public void testCreateDirectoryDetection() throws Exception {
        start();

        createDirectory("dir");

        listener.waitForCheck();

        assertEquals(1, listener.getCreatedDirectories().size());

        stop();
    }

    public void testDeleteFileDetection() throws Exception {
        start();

        final File file = writeFile("file", "file");

        assertTrue("file should exist", file.exists());

        listener.waitForCheck();

        assertEquals("expecting 1 file created", 1, listener.getCreatedFiles().size());
        //assertEquals("expecting 0 directories changed", 0, listener.getChangedDirectories().size()); // todo investigate why this is failing on Windows

        file.delete();
        assertFalse("file should not exist", file.exists());

        listener.waitForCheck();

        assertEquals("expecting 1 file deleted", 1, listener.getDeletedFiles().size());

        stop();
    }
    public void testDeleteDirectoryDetection() throws Exception {
        start();

        final File dir = createDirectory("dir");
        createDirectory("dir/sub");
        final File file = writeFile("dir/sub/file", "file");

        listener.waitForCheck();

        assertEquals(2, listener.getCreatedDirectories().size());
        assertEquals(1, listener.getCreatedFiles().size());

        delay();

        FileUtils.deleteDirectory(dir);
        assertTrue(!dir.exists());
        assertTrue(!file.exists());

        listener.waitForCheck();

        assertEquals(2, listener.getDeletedDirectories().size());
        assertEquals(1, listener.getDeletedFiles().size());

        stop();
    }

    public void testModifyFileDetection() throws Exception {
        start();

        writeFile("file", "file");

        listener.waitForCheck();

        assertEquals(1, listener.getCreatedFiles().size());

        delay();

        writeFile("file", "changed file");

        listener.waitForCheck();

        assertEquals(1, listener.getChangedFiles().size());

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

        assertEquals(directory.lastModified(), modified);
    }

    public void testCreatingFileInSubDirectoryChangesLastModified() throws Exception {
        createDirectory("dir");

        final long modified = directory.lastModified();

        delay();

        writeFile("dir/file", "file");

        assertEquals(directory.lastModified(), modified);
    }

    public void testInterval() throws Exception {

        final long interval = 1000;

        start();
        fam.setInterval(interval);

        listener.waitForCheck();
        final long t1 = System.currentTimeMillis();

        listener.waitForCheck();
        final long t2 = System.currentTimeMillis();

        final long diff = t2-t1;

        // interval should be at around the same interval
        assertTrue("the interval was set to " + interval + " but the time difference was " + diff, (diff > (interval-50)) && (diff < (interval+50)));

        stop();
    }
}
