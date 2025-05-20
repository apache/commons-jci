/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.jci2.core.stores;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.file.PathUtils;

/**
 * Stores the results on disk
 */
public final class FileResourceStore implements ResourceStore {

    private final File root;

    public FileResourceStore(final File pFile) {
        root = pFile;
    }

    @Override
    public byte[] read(final String pResourceName) {
        try {
            return Files.readAllBytes(getPath(pResourceName));
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public void write(final String pResourceName, final byte[] pData) {
        try {
            final Path path = getPath(pResourceName);
            PathUtils.createParentDirectories(path);
            Files.write(path, pData);
        } catch (final Exception e) {
            // FIXME: now what?
        }
    }

    @Override
    public void remove(final String pResourceName) {
        getFile(pResourceName).delete();
    }

    private File getFile(final String pResourceName) {
        return getPath(pResourceName).toFile();
    }

    private Path getPath(final String pResourceName) {
        final String fileName = pResourceName.replace('/', File.separatorChar);
        return Paths.get(root.toString(), fileName);
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
    private void list(final File pFile, final List<String> pFiles) {
        if (pFile.isDirectory()) {
            final File[] directoryFiles = pFile.listFiles();
            for (final File directoryFile : directoryFiles) {
                list(directoryFile, pFiles);
            }
        } else {
            pFiles.add(pFile.getAbsolutePath().substring(root.getAbsolutePath().length() + 1));
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + root.toString();
    }
}
