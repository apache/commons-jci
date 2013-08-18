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

package org.apache.commons.jci.listeners;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AbstractFilesystemAlterationListener provides some convenience methods helping to
 * implement a FilesystemAlterationListener.
 * @author tcurdt
 */
public abstract class AbstractFilesystemAlterationListener implements FilesystemAlterationListener {

    private final Log log = LogFactory.getLog(AbstractFilesystemAlterationListener.class);

    private final Collection<File> createdFiles = new ArrayList<File>();
    private final Collection<File> changedFiles = new ArrayList<File>();
    private final Collection<File> deletedFiles = new ArrayList<File>();
    private final Collection<File> createdDirectories = new ArrayList<File>();
    private final Collection<File> changedDirectories = new ArrayList<File>();
    private final Collection<File> deletedDirectories = new ArrayList<File>();

    
    private final static class Signal {
        public boolean triggered;
    }

    private final Signal eventSignal = new Signal();
    private final Signal checkSignal = new Signal();
    
    protected FilesystemAlterationObserver observer;

    public void onDirectoryCreate( final File pDir ) {
        createdDirectories.add(pDir);
    }
    public void onDirectoryChange( final File pDir ) {
        changedDirectories.add(pDir);
    }
    public void onDirectoryDelete( final File pDir ) {
        deletedDirectories.add(pDir);
    }

    public void onFileCreate( final File pFile) {
        createdFiles.add(pFile);
    }
    public void onFileChange( final File pFile ) {
        changedFiles.add(pFile);
    }
    public void onFileDelete( final File pFile ) {
        deletedFiles.add(pFile);
    }


    public Collection<File> getChangedDirectories() {
        return changedDirectories;
    }

    public Collection<File> getChangedFiles() {
        return changedFiles;
    }

    public Collection getCreatedDirectories() {
        return createdDirectories;
    }

    public Collection<File> getCreatedFiles() {
        return createdFiles;
    }

    public Collection<File> getDeletedDirectories() {
        return deletedDirectories;
    }

    public Collection<File> getDeletedFiles() {
        return deletedFiles;
    }

    protected void signals() {
        if (createdFiles.size() > 0 || createdDirectories.size() > 0 ||
            changedFiles.size() > 0 || changedDirectories.size() > 0 ||
            deletedFiles.size() > 0 || deletedDirectories.size() > 0) {

            log.debug("event signal");

            synchronized(eventSignal) {
                eventSignal.triggered = true;
                eventSignal.notifyAll();
            }
        }

        log.debug("check signal");

        synchronized(checkSignal) {
            checkSignal.triggered = true;
            checkSignal.notifyAll();
        }
    }

    public void onStart( final FilesystemAlterationObserver pObserver ) {
        observer = pObserver;

        createdFiles.clear();
        changedFiles.clear();
        deletedFiles.clear();
        createdDirectories.clear();
        changedDirectories.clear();
        deletedDirectories.clear();
    }

    public void onStop( final FilesystemAlterationObserver pObserver ) {
        signals();
        observer = null;
    }
        
    public void waitForEvent() throws Exception {
        synchronized(eventSignal) {
            eventSignal.triggered = false;
        }
        log.debug("waiting for change");
        if (!waitForSignal(eventSignal, 10)) {
            throw new Exception("timeout");
        }
    }
    
    /**
     * we don't reset the signal so if there was a check it is
     * already true and exit immediatly otherwise it will behave just
     * like waitForCheck()
     * 
     * @throws Exception in case of a timeout
     */
    public void waitForFirstCheck() throws Exception {
        log.debug("waiting for first check");
        if (!waitForSignal(checkSignal, 10)) {
            throw new Exception("timeout");
        }        
    }

    /**
     * wait for the next filesystem check to happen
     * 
     * @throws Exception in case of a timeout
     */
    public void waitForCheck() throws Exception {
        synchronized(checkSignal) {
            checkSignal.triggered = false;
        }
        log.debug("waiting for check");
        if (!waitForSignal(checkSignal, 10)) {
            throw new Exception("timeout");
        }
    }
    
    private boolean waitForSignal(final Signal pSignal, final int pSecondsTimeout) {
        int i = 0;
        while(true) {
            synchronized(pSignal) {
                if (!pSignal.triggered) {
                    try {
                        pSignal.wait(1000);
                    } catch (InterruptedException e) {
                    }

                    if (++i > pSecondsTimeout) {
                        log.error("timeout after " + pSecondsTimeout + "s");
                        return false;
                    }
                    
                } else {
                    pSignal.triggered = false;
                    break;
                }
            }
        }        
        return true;
    }

}
