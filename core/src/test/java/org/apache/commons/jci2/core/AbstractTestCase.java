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

package org.apache.commons.jci2.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 *
 * @author tcurdt
 */
public abstract class AbstractTestCase extends TestCase {

    private final Log log = LogFactory.getLog(AbstractTestCase.class);

    protected File directory;

    @Override
    protected void setUp() throws Exception {

        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");

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

    protected void delay() {
        try {
            Thread.sleep(1500);
        } catch (final InterruptedException e) {
        }
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

    @Override
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(directory);
    }
}
