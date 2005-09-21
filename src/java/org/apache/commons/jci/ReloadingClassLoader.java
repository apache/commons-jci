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
import org.apache.commons.jci.listeners.AbstractListener;
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
    //private final Collection reloadingListeners = new HashSet();
    private ResourceStore[] stores = new ResourceStore[0];
    private ClassLoader delegate;
    
    public ReloadingClassLoader(final ClassLoader pParent) {        
        super(pParent);
        parent = pParent;        

        delegate = new ResourceStoreClassLoader(parent, stores);
    }

    public void addListener(final AbstractListener pListener) {
        pListener.setReloadingClassLoader(this);
        addResourceStore(pListener.getStore());
    }
    
    public void removeListener(final AbstractListener pListener) {
        removeResourceStore(pListener.getStore());
        pListener.setReloadingClassLoader(null);
    }
    
    private void addResourceStore(final ResourceStore pStore) {
        final int n = stores.length;
        final ResourceStore[] newStores = new ResourceStore[n + 1];
        System.arraycopy(stores, 0, newStores, 0, n);
        newStores[n] = pStore;
        stores = newStores;
        delegate = new ResourceStoreClassLoader(parent, stores);
    }

    private void removeResourceStore(final ResourceStore pStore) {
        //FIXME
    }
    
    /*
    public void start() {
        fam = new FilesystemAlterationMonitor(); 
        fam.addListener(new ReloadingListener(store) {  
            protected void needsReload( boolean pReload ) {
                super.needsReload(pReload);
                if (pReload) {
                    ReloadingClassLoader.this.reload();                    
                } else {
                    ReloadingClassLoader.this.notifyReloadingListeners(false);                    
                }
            }
        }, repository);
        fam.start();
    }
    
    public void stop() {
        fam.stop();
    }
    */
    /*
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
    */
    public void reload(final boolean pReload) {
        if (pReload) {
            log.debug("reloading");
            delegate = new ResourceStoreClassLoader(parent, stores);
            //notifyReloadingListeners(true);
        } else {
            log.debug("not reloading");
            //notifyReloadingListeners(false);            
        }
    }
    /*
    private void notifyReloadingListeners(final boolean pReload) { 
        synchronized (reloadingListeners) {
            for (final Iterator it = reloadingListeners.iterator(); it.hasNext();) {
                final ReloadingClassLoaderListener listener = (ReloadingClassLoaderListener) it.next();
                listener.hasReloaded(pReload);
            }            
        }
    }
    */
    public static String clazzName( final File base, final File file ) {
        final int rootLength = base.getAbsolutePath().length();
        final String absFileName = file.getAbsolutePath();
        final int p = absFileName.lastIndexOf('.');
        final String relFileName = absFileName.substring(rootLength + 1, p);
        final String clazzName = relFileName.replace(File.separatorChar, '.');
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
