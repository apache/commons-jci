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

package org.apache.commons.jci2.fam.monitor;

import java.io.File;

/**
 * A listener that receives events of file system modifications. The observer basically represents the source of the events. The file root and its state.
 *
 * @see FilesystemAlterationObserver
 */
public interface FilesystemAlterationListener {

    /**
     * Receives notification that we are starting to observe.
     *
     * @param observer the observer.
     */
    void onStart(final FilesystemAlterationObserver observer);

    /**
     * Receives notification that a file was created.
     *
     * @param file the file.
     */
    void onFileCreate(final File file);

    /**
     * Receives notification that a file was changed.
     *
     * @param file the file.
     */
    void onFileChange(final File file);

    /**
     * Receives notification that a file was deleted.
     *
     * @param file the file.
     */
    void onFileDelete(final File file);

    /**
     * Receives notification that a directory was created.
     *
     * @param directory the directory.
     */
    void onDirectoryCreate(final File directory);

    /**
     * Receives notification that a directory was changed.
     *
     * @param directory the directory.
     */
    void onDirectoryChange(final File directory);

    /**
     * Receives notification that a directory was deleted.
     *
     * @param directory the directory.
     */
    void onDirectoryDelete(final File directory);

    /**
     * Receives notification that we are stopping to observe.
     *
     * @param observer the observer.
     */
    void onStop(final FilesystemAlterationObserver observer);
}
