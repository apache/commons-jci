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
package org.apache.jci.monitor;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author tcurdt
 *
 */
public final class AlterationMonitor implements Runnable {
    
    private final static Log log = LogFactory.getLog(AlterationMonitor.class);
    
    public class Entry {
        private final File file;
        private long lastModified;
        private Set paths = new HashSet();
        private Set childs = new HashSet();
        private final boolean isDirectory;
        
        public Entry( final File pFile ) {
            file = pFile;
            lastModified = -1;
            isDirectory = file.isDirectory();
        }
        
        public boolean hasChanged() {
            return file.lastModified() != lastModified;
        }

        public boolean isDelected() {
            return !file.exists();
        }
        
        public boolean isDirectory() {
            return isDirectory;
        }
        
        public Entry[] getChilds() {
            final Entry[] r = new Entry[childs.size()];
            childs.toArray(r);
            return r;
        }

        private FileFilter getFileFilter() {
            return new FileFilter() {
                public boolean accept(final File pathname) {                    
                    final String p = pathname.getAbsolutePath();                    
                    return !paths.contains(p);
                }
            };
        }
        
        public Entry[] getNonChilds() {
            final File[] newFiles = file.listFiles(getFileFilter());
            final Entry[] r = new Entry[newFiles.length];
            for (int i = 0; i < newFiles.length; i++) {
                r[i] = new Entry(newFiles[i]);
            }
            return r;
        }
        
        public void add( final Entry entry ) {
            childs.add(entry);
            paths.add(entry.toString());
            onCreate(entry);
        }
        
        private void deleteChilds() {
            final Entry[] childs = this.getChilds();
            for (int i = 0; i < childs.length; i++) {
                final Entry child = childs[i];
                delete(child);                
            }
        }
        
        public void delete( final Entry entry ) {
            childs.remove(entry);
            paths.remove(entry.toString());
            entry.deleteChilds();
            onDelete(entry);
        }
        
        public File getFile() {
            return file;
        }
        
        public void markNotChanged() {
            lastModified = file.lastModified();
        }
        
        public String toString() {
            return file.getAbsolutePath();
        }
    }
    
    
    private final Entry root;
    
    private final Set listeners = new HashSet();
    private long delay = 3000;
    private boolean running = true;

    public AlterationMonitor( final File pDirectory ) {
        root = new Entry(pDirectory);
    }
    
    public File getRoot() {
        return root.getFile();
    }
    
    public void setInterval( final long pDelay ) {
        delay = pDelay;
    }
    
    public void addListener( final AlterationListener listener ) {
        listeners.add(listener);
    }
    
    public void removeListener( final AlterationListener listener ) {
        listeners.remove(listener);
    }

    private void onStart() {
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            final AlterationListener listener = (AlterationListener) it.next();
            listener.onStart();
        }
    }

    private void onStop() {
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            final AlterationListener listener = (AlterationListener) it.next();
            listener.onStop();
        }
    }
    
    private void onCreate( final Entry entry ) {
        
        if (entry.isDirectory()) {
            log.debug("* created dir " + entry);
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                final AlterationListener listener = (AlterationListener) it.next();
                listener.onCreateDirectory(entry.getFile());
            }
        }
        else {
            log.debug("* created file " + entry);
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                final AlterationListener listener = (AlterationListener) it.next();
                listener.onCreateFile(entry.getFile());
            }
        }

        entry.markNotChanged();
    }
    private void onChange( final Entry entry ) {
        if (entry.isDirectory()) {
            log.debug("* changed dir " + entry);
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                final AlterationListener listener = (AlterationListener) it.next();
                listener.onChangeDirectory(entry.getFile());
            }
        }
        else {
            log.debug("* changed file " + entry);            
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                final AlterationListener listener = (AlterationListener) it.next();
                listener.onChangeFile(entry.getFile());
            }
        }
        entry.markNotChanged();
    }
    private void onDelete( final Entry entry ) {
        if (entry.isDirectory()) {
            log.debug("* deleted dir " + entry);
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                final AlterationListener listener = (AlterationListener) it.next();
                listener.onDeleteDirectory(entry.getFile());
            }
        }
        else {
            log.debug("* deleted file " + entry);            
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                final AlterationListener listener = (AlterationListener) it.next();
                listener.onDeleteFile(entry.getFile());
            }
        }
    }
    
    private void check( final Entry entry, final boolean create ) {
        if (entry.isDirectory()) {

            final Entry[] currentChilds = entry.getChilds();

            if (entry.hasChanged() || create) {
                if (!create) {
                    onChange(entry);
                
	                for (int i = 0; i < currentChilds.length; i++) {
	                    final Entry child = currentChilds[i];
	                    
	                    if (child.isDelected()) {
	                        entry.delete(child);
	                        currentChilds[i] = null;
	                    }
	                }
                }
                
                final Entry[] newChilds = entry.getNonChilds();
                for (int i = 0; i < newChilds.length; i++) {
                    final Entry child = newChilds[i];
                    entry.add(child);
                }

                if (!create) {
	                for (int i = 0; i < currentChilds.length; i++) {
	                    final Entry child = currentChilds[i];
	                    if (child != null) {
	                        check(child, false);                        
	                    }
	                }
                }

                for (int i = 0; i < newChilds.length; i++) {
                    final Entry child = newChilds[i];
                    check(child, true);
                }

            }
            else {
                for (int i = 0; i < currentChilds.length; i++) {
                    final Entry child = currentChilds[i];
                    check(child, false);
                }
                
            }

        }
        else {
            if (entry.isDelected()) {
                throw new RuntimeException("should not get here");
            }
            else if (entry.hasChanged()) {
                onChange(entry);
            }            
        }

            
    }
    
    public void run() {
        while(running) {
	        //log.debug("*");

	        if (!root.isDelected()) {
		        onStart();
	            check(root, false);
		        onStop();
	        }
	        
	        try {
	            Thread.sleep(delay);
	        } catch (InterruptedException e) {
	        }
        }
    }
    
}
