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
package org.apache.commons.jci.stores;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;


/**
 * @author tcurdt
 */
public final class FileResourceStore implements ResourceStore {

    private final File root;

    public FileResourceStore( final File pFile ) {
        root = pFile;
    }
    
    public byte[] read( final String pResourceName ) {
        InputStream is = null;
        try {
            is = new FileInputStream(getFile(pResourceName));
            final byte[] data = IOUtils.toByteArray(is);
            return data;
        } catch (Exception e) {
        	return null;
        } finally {
        	IOUtils.closeQuietly(is);
        }
    }
    
    public void write( final String pResourceName, final byte[] pData ) {
        OutputStream os = null;
        try {
            final File file = getFile(pResourceName);
            final File parent = file.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException("could not create" + parent);
                }
            }
            os = new FileOutputStream(file);
            os.write(pData);
        } catch (Exception e) {
        	// FIXME: now what?
        } finally {
        	IOUtils.closeQuietly(os);
        }
    }

    public void remove( final String pResourceName ) {
        getFile(pResourceName).delete();
    }

    private File getFile( final String pResourceName ) {
        final String fileName = pResourceName.replace('/', File.separatorChar);
        return new File(root, fileName);
    }

    /**
     * @deprecated
     */
    public String[] list() {
        final List files = new ArrayList();
        list(root, files);
        return (String[]) files.toArray(new String[files.size()]);
    }

    /**
     * @deprecated
     */
    private void list(final File pFile, final List pFiles) {
        if (pFile.isDirectory()) {
            final File[] directoryFiles = pFile.listFiles();
            for (int i=0; i < directoryFiles.length; i++) {
                list(directoryFiles[i], pFiles);
            }
        } else {
            pFiles.add(pFile.getAbsolutePath().substring(root.getAbsolutePath().length()+1));
        }
    }
    
    public String toString() {
        return this.getClass().getName() + root.toString();
    }

}
