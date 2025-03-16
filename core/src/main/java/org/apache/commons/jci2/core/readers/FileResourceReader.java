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

package org.apache.commons.jci2.core.readers;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * A simple file system based Reader implementation
 */
public final class FileResourceReader implements ResourceReader {

    private final File root;

    public FileResourceReader( final File pRoot ) {
        root = pRoot;
    }

    @Override
    public boolean isAvailable( final String pResourceName ) {
        return new File(root, pResourceName).exists();
    }

    @Override
    public byte[] getBytes(final String pResourceName) {
        try {
            return FileUtils.readFileToString(new File(root, pResourceName), StandardCharsets.UTF_8).getBytes();
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String[] list() {
        final List<String> files = new ArrayList<>();
        list(root, files);
        return files.toArray(new String[0]);
    }

    /**
     * @deprecated
     */
    @Deprecated
    private void list( final File pFile, final List<String> pFiles ) {
        if (pFile.isDirectory()) {
            final File[] directoryFiles = pFile.listFiles();
            for (final File directoryFile : directoryFiles) {
                list(directoryFile, pFiles);
            }
        } else {
            pFiles.add(pFile.getAbsolutePath().substring(root.getAbsolutePath().length()+1));
        }
    }
}
