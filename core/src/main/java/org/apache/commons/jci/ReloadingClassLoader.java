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
package org.apache.commons.jci;

import java.io.InputStream;
import java.net.URL;
import org.apache.commons.jci.listeners.NotificationListener;
import org.apache.commons.jci.listeners.ReloadingListener;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.stores.ResourceStoreClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tcurdt
 */
public class ReloadingClassLoader extends ClassLoader implements NotificationListener {
    
    private final Log log = LogFactory.getLog(ReloadingClassLoader.class);
    
    private final ClassLoader parent;
    //private final Collection reloadingListeners = new HashSet();
    private ResourceStore[] stores = new ResourceStore[0];
    private ClassLoader delegate;
    
    public ReloadingClassLoader( final ClassLoader pParent ) {        
        super(pParent);
        parent = pParent;        

        delegate = new ResourceStoreClassLoader(parent, stores);
    }

    public void addListener(final ReloadingListener pListener) {
        pListener.setNotificationListener(this);
        addResourceStore(pListener.getStore());
    }
    
    public void removeListener(final ReloadingListener pListener) {
        removeResourceStore(pListener.getStore());
        pListener.setNotificationListener(null);
    }
    
    private boolean addResourceStore( final ResourceStore pStore ) {
        try {        
            final int n = stores.length;
            final ResourceStore[] newStores = new ResourceStore[n + 1];
            System.arraycopy(stores, 0, newStores, 0, n);
            newStores[n] = pStore;
            stores = newStores;
            delegate = new ResourceStoreClassLoader(parent, stores);
            return true;
        } catch ( final Exception e ) {
            // TODO: rethrow?
        }
        return false;
    }

    private boolean removeResourceStore( final ResourceStore pStore ) {
        try {
            final int n = stores.length;
            int i = 0;
                        
            //find the pStore and index position with var i
            while ( ( i <= n )  && ( stores[i] != pStore ) ) {
                i++;
            }
            
            
            //pStore was not found
            if ( i == n ) {
                throw new Exception( "pStore was not found" );
            }
            
            // if stores length > 1 then array copy old values, else create new empty store 
            if (n > 1) {            
                final ResourceStore[] newStores = new ResourceStore[n - 1];
                
                System.arraycopy(stores, 0, newStores, 0, i-1);
                System.arraycopy(stores, i, newStores, i, newStores.length - 1);
                
                stores = newStores;
                delegate = new ResourceStoreClassLoader(parent, stores);
            } else {
                stores = new ResourceStore[0];
            }
            return true;
            
        } catch ( final Exception e ) {
            // TODO: re-throw?
        }
                
        return false;
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
    public void handleNotification() {
        log.debug("reloading");
        delegate = new ResourceStoreClassLoader(parent, stores);
        //notifyReloadingListeners();
    }
    /*
    private void notifyReloadingListeners() { 
        synchronized (reloadingListeners) {
            for (final Iterator it = reloadingListeners.iterator(); it.hasNext();) {
                final ReloadingClassLoaderListener listener = (ReloadingClassLoaderListener) it.next();
                listener.hasReloaded();
            }            
        }
    }
    */
    
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
