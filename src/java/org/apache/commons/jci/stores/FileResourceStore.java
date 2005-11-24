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
package org.apache.commons.jci.stores;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;


/**
 * @author tcurdt
 *
 */
public final class FileResourceStore implements ResourceStore {

    private final File root;

    public FileResourceStore(final File pFile) {
        root = pFile;
    }
    public byte[] read( final String resourceName ) {
        InputStream is = null;
        try {
            is = new FileInputStream(getFile(resourceName));
            final byte[] data = IOUtils.toByteArray(is);
            return data;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }
    public void write( final String resourceName, final byte[] clazzData ) {
        OutputStream os = null;
        try {
            final File file = getFile(resourceName);
            final File parent = file.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IOException("could not create" + parent);
                }
            }
            os = new FileOutputStream(file);
            os.write(clazzData);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void remove( final String pResourceName ) {
        getFile(pResourceName).delete();
    }

    private File getFile(final String pResourceName) {
        final String fileName = pResourceName.replace('.', File.separatorChar) + ".class";
        return new File(root, fileName);
    }

    public String[] list() {
        final List files = new ArrayList();
        list(root, files);
        return (String[]) files.toArray(new String[files.size()]);
    }

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
