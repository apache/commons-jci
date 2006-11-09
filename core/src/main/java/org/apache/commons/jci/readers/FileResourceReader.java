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
package org.apache.commons.jci.readers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * @author tcurdt
 */
public final class FileResourceReader implements ResourceReader {

    private final File root;

    public FileResourceReader( final File pRoot ) {
        root = pRoot;        
    }
    
    public boolean isAvailable( final String pResourceName ) {
        return new File(root, pResourceName).exists();
    }

    public byte[] getBytes( final String pResourceName ) {
        try {
            return FileUtils.readFileToString(new File(root, pResourceName), "UTF-8").getBytes();
        } catch(Exception e) {
        	// TODO
        }
        return null;
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
    private void list( final File pFile, final List pFiles ) {
        if (pFile.isDirectory()) {
            final File[] directoryFiles = pFile.listFiles();
            for (int i = 0; i < directoryFiles.length; i++) {
                list(directoryFiles[i], pFiles);
            }
        } else {
            pFiles.add(pFile.getAbsolutePath().substring(root.getAbsolutePath().length()+1));
        }
    }   
}
