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
package org.apache.commons.jci;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.commons.jci.listeners.ReloadingListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.stores.ResourceStoreClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tcurdt
 *
 */
public class ReloadingClassLoader extends ClassLoader {
    
    private final static Log log = LogFactory.getLog(ReloadingClassLoader.class);
    
    private final ClassLoader parent;
    private final ResourceStore store;
    private final Collection reloadingListeners = new HashSet();

    private ClassLoader delegate;

    protected final ResourceReader reader;
    protected final File repository;

    protected FilesystemAlterationMonitor fam;
    protected Thread thread;
    
    public ReloadingClassLoader(final ClassLoader pParent, final File pRepository) {
        this(pParent, pRepository, new MemoryResourceStore());
    }

    public ReloadingClassLoader(final ClassLoader pParent, final File pRepository, final ResourceStore pStore) {        
        super(pParent);

        parent = pParent;        
        repository = pRepository;        
        reader = new FileResourceReader(repository);
        store = pStore;
                
        delegate = new ResourceStoreClassLoader(parent, store);
    }
    
    public void start() {
        fam = new FilesystemAlterationMonitor(); 
        fam.addListener(new ReloadingListener(store) {
            public void reload() {
                ReloadingClassLoader.this.reload();
            }            
        }, repository);
        thread = new Thread(fam);         
        thread.start();
        reload();        
    }

    public void stop() {
        fam.stop();
        try {
            thread.join();
        } catch (InterruptedException e) {
            ;
        }
    }

    public void addListener(final ReloadingClassLoaderListener pListener) {
        synchronized (reloadingListeners) {
            reloadingListeners.add(pListener);
        }                
    }
    
    public boolean removeListener(final ReloadingClassLoaderListener pListener) {
        synchronized (reloadingListeners) {
            return reloadingListeners.remove(pListener);
        }        
    }
    
    protected void reload() {
        log.debug("reloading");
        delegate = new ResourceStoreClassLoader(parent, store);
        synchronized (reloadingListeners) {
            for (final Iterator it = reloadingListeners.iterator(); it.hasNext();) {
                final ReloadingClassLoaderListener listener = (ReloadingClassLoaderListener) it.next();
                listener.reload();
            }            
        }
    }
    
    public static String clazzName( final File base, final File file ) {
        final int rootLength = base.getAbsolutePath().length();
        final String absFileName = file.getAbsolutePath();
        final String relFileName = absFileName.substring(
                rootLength + 1,
                absFileName.length() - ".java".length()
                );
        final String clazzName = relFileName.replace(File.separatorChar,'.');
        return clazzName;
    }


    public void clearAssertionStatus() {
        delegate.clearAssertionStatus();
    }
    public URL getResource(String name) {
        return delegate.getResource(name);
    }
    public InputStream getResourceAsStream(String name) {
        return delegate.getResourceAsStream(name);
    }
    public Class loadClass(String name) throws ClassNotFoundException {
        return delegate.loadClass(name);
    }
    public void setClassAssertionStatus(String className, boolean enabled) {
        delegate.setClassAssertionStatus(className, enabled);
    }
    public void setDefaultAssertionStatus(boolean enabled) {
        delegate.setDefaultAssertionStatus(enabled);
    }
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        delegate.setPackageAssertionStatus(packageName, enabled);
    }
}
