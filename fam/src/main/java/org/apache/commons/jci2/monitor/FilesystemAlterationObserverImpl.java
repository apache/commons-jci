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

package org.apache.commons.jci2.monitor;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of a FilesystemAlterationObserver
 * 
 * @author tcurdt
 */
public class FilesystemAlterationObserverImpl implements FilesystemAlterationObserver {

    private final Log log = LogFactory.getLog(FilesystemAlterationObserverImpl.class);
    
    private interface MonitorFile {

        long lastModified();
        MonitorFile[] listFiles();
        boolean isDirectory();
        boolean exists();
        String getName();

    }
    
    private final static class MonitorFileImpl implements MonitorFile {

        private final File file;

        public MonitorFileImpl( final File pFile ) {
            file = pFile;
        }

        public boolean exists() {
            return file.exists();
        }

        public MonitorFile[] listFiles() {
            final File[] children = file.listFiles();
            if (children == null) { // not a directory or IOError (e.g. protection issue)
                return new MonitorFile[0];
            }

            final MonitorFile[] providers = new MonitorFile[children.length];
            for (int i = 0; i < providers.length; i++) {
                providers[i] = new MonitorFileImpl(children[i]);
            }
            return providers;
        }

        public String getName() {
            return file.getName();
        }

        public boolean isDirectory() {
            return file.isDirectory();
        }

        public long lastModified() {
            return file.lastModified();
        }

        @Override
        public String toString() {
            return file.toString();
        }

    }

    private final class Entry {

        private final static int TYPE_UNKNOWN = 0;
        private final static int TYPE_FILE = 1;
        private final static int TYPE_DIRECTORY = 2;

        private final MonitorFile file;
        private long lastModified = -1;
        private int lastType = TYPE_UNKNOWN;
        private final Map<String, Entry> children = new HashMap<String, Entry>();

        public Entry(final MonitorFile pFile) {
            file = pFile;
        }

        public String getName() {
            return file.getName();
        }
        
        
        @Override
        public String toString() {
            return file.toString();
        }


        private void compareChildren() {
            if (!file.isDirectory()) {
                return;
            }

            final MonitorFile[] files = file.listFiles();
            final Set<Entry> deleted = new HashSet<Entry>(children.values());
            for (MonitorFile f : files) {
                final String name = f.getName();
                final Entry entry = children.get(name);
                if (entry != null) {
                    // already recognized as child
                    deleted.remove(entry);

                    if(entry.needsToBeDeleted()) {
                        // we have to delete this one
                        children.remove(name);
                    }
                } else {
                    // a new child
                    final Entry newChild = new Entry(f);
                    children.put(name, newChild);
                    newChild.needsToBeDeleted();
                }
            }

            // the ones not found on disk anymore

            for (Entry entry : deleted) {
                entry.deleteChildrenAndNotify();
                children.remove(entry.getName());
            }
        }


        private void deleteChildrenAndNotify() {
            for (Entry entry : children.values()) {
                entry.deleteChildrenAndNotify();
            }
            children.clear();

            if(lastType == TYPE_DIRECTORY) {
                notifyOnDirectoryDelete(this);
            } else if (lastType == TYPE_FILE) {
                notifyOnFileDelete(this);
            }
        }

        public boolean needsToBeDeleted() {

            if (!file.exists()) {
                // deleted or has never existed yet

//                log.debug(file + " does not exist or has been deleted");

                deleteChildrenAndNotify();

                // mark to be deleted by parent
                return true;
            } else {
                // exists
                final long currentModified = file.lastModified(); 

                if (currentModified != lastModified) {
                    // last modified has changed
                    lastModified = currentModified;

//                    log.debug(file + " has new last modified");

                    // types only changes when also the last modified changes
                    final int newType = (file.isDirectory()?TYPE_DIRECTORY:TYPE_FILE); 

                    if (lastType != newType) {
                        // the type has changed

//                        log.debug(file + " has a new type");

                        deleteChildrenAndNotify();

                        lastType = newType;

                        // and then an add as the new type

                        if (newType == TYPE_DIRECTORY) {
                            notifyOnDirectoryCreate(this);
                            compareChildren();
                        } else {
                            notifyOnFileCreate(this);
                        }

                        return false;
                    }

                    if (newType == TYPE_DIRECTORY) {
                        notifyOnDirectoryChange(this);
                        compareChildren();
                    } else {
                        notifyOnFileChange(this);
                    }

                    return false;

                } else {

                    // so exists and has not changed

//                    log.debug(file + " does exist and has not changed");

                    compareChildren();

                    return false;
                }
            }
        }
        
        public MonitorFile getFile() {
            return file;
        }

        public void markNotChanged() {
            lastModified = file.lastModified();
        }

    }

    private final File rootDirectory;
    private final Entry rootEntry;

    private FilesystemAlterationListener[] listeners = new FilesystemAlterationListener[0];
    private final Set<FilesystemAlterationListener> listenersSet = new HashSet<FilesystemAlterationListener>();

    public FilesystemAlterationObserverImpl( final File pRootDirectory ) {
        rootDirectory = pRootDirectory;
        rootEntry = new Entry(new MonitorFileImpl(pRootDirectory));
    }



    private void notifyOnStart() {
        log.debug("onStart " + rootEntry);
        for (FilesystemAlterationListener listener : listeners) {
            listener.onStart(this);
        }
    }
    private void notifyOnStop() {
        log.debug("onStop " + rootEntry);
        for (FilesystemAlterationListener listener : listeners) {
            listener.onStop(this);
        }
    }

    private void notifyOnFileCreate( final Entry pEntry ) {
        log.debug("onFileCreate " + pEntry);
        for (FilesystemAlterationListener listener : listeners) {
            listener.onFileCreate(((MonitorFileImpl)pEntry.getFile()).file );
        }
    }
    private void notifyOnFileChange( final Entry pEntry ) {
        log.debug("onFileChange " + pEntry);
        for (FilesystemAlterationListener listener : listeners) {
            listener.onFileChange(((MonitorFileImpl)pEntry.getFile()).file );
        }
    }
    private void notifyOnFileDelete( final Entry pEntry ) {
        log.debug("onFileDelete " + pEntry);
        for (FilesystemAlterationListener listener : listeners) {
            listener.onFileDelete(((MonitorFileImpl)pEntry.getFile()).file );
        }
    }

    private void notifyOnDirectoryCreate( final Entry pEntry ) {
        log.debug("onDirectoryCreate " + pEntry);
        for (FilesystemAlterationListener listener : listeners) {
            listener.onDirectoryCreate(((MonitorFileImpl)pEntry.getFile()).file );
        }
    }
    private void notifyOnDirectoryChange( final Entry pEntry ) {
        log.debug("onDirectoryChange " + pEntry);
        for (FilesystemAlterationListener listener : listeners) {
            listener.onDirectoryChange(((MonitorFileImpl)pEntry.getFile()).file );
        }
    }
    private void notifyOnDirectoryDelete( final Entry pEntry ) {
        log.debug("onDirectoryDelete " + pEntry);
        for (FilesystemAlterationListener listener : listeners) {
            listener.onDirectoryDelete(((MonitorFileImpl)pEntry.getFile()).file );
        }
    }


    private void checkEntries() {
        if(rootEntry.needsToBeDeleted()) {
            // root not existing
            rootEntry.lastType = Entry.TYPE_UNKNOWN;
        }
    }

    
    public void checkAndNotify() {
    	synchronized(listenersSet) {
	        if (listeners.length == 0) {
	            return;
	        }
	
	        notifyOnStart();
	        
	        checkEntries();
	        
	        notifyOnStop();
    	}
    }

    
    public File getRootDirectory() {
        return rootDirectory;
    }

    public void addListener( final FilesystemAlterationListener pListener ) {
    	synchronized(listenersSet) {
	        if (listenersSet.add(pListener)) {
	            listeners = createArrayFromSet();
	        }
    	}
    }

    public void removeListener( final FilesystemAlterationListener pListener ) {
    	synchronized(listenersSet) {
	        if (listenersSet.remove(pListener)) {
	            listeners = createArrayFromSet();
	        }
    	}
    }

    private FilesystemAlterationListener[] createArrayFromSet() {
        final FilesystemAlterationListener[] newListeners = new FilesystemAlterationListener[listenersSet.size()];
        listenersSet.toArray(newListeners);
        return newListeners;
    }

    public FilesystemAlterationListener[] getListeners() {
    	synchronized(listenersSet) {
        	final FilesystemAlterationListener[] res = new FilesystemAlterationListener[listeners.length];
        	System.arraycopy(listeners, 0, res, 0, res.length);
            return res;
    	}    	
    }
}
