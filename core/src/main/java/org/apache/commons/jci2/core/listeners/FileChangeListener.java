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

package org.apache.commons.jci2.core.listeners;

import java.io.File;

import org.apache.commons.jci2.fam.listeners.AbstractFilesystemAlterationListener;
import org.apache.commons.jci2.fam.monitor.FilesystemAlterationObserver;

/**
 * The most simple implemenation of an FilesystemAlterationListener.
 */
public class FileChangeListener extends AbstractFilesystemAlterationListener {

    private boolean changed;

    public boolean hasChanged() {
        return changed;
    }

    @Override
    public void onStart( final FilesystemAlterationObserver pObserver ) {
        changed = false;
        super.onStart(pObserver);
    }

    @Override
    public void onStop( final FilesystemAlterationObserver pObserver ) {
        super.onStop(pObserver);
    }

    @Override
    public void onFileChange( final File pFile ) {
        changed = true;
    }

    @Override
    public void onFileCreate( final File pFile ) {
        changed = true;
    }

    @Override
    public void onFileDelete( final File pFile ) {
        changed = true;
    }

    @Override
    public void onDirectoryChange( final File pDir ) {
    }

    @Override
    public void onDirectoryCreate( final File pDir ) {
    }

    @Override
    public void onDirectoryDelete( final File pDir ) {
    }

}
