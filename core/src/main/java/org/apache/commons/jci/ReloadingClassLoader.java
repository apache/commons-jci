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

import org.apache.commons.jci.listeners.ReloadNotificationListener;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.stores.ResourceStoreClassLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The ReloadingClassLoader uses a delegation mechansim to allow
 * classes to be reloaded. That means that loadClass calls may
 * return different results if the class was changed in the underlying
 * ResourceStore.
 * 
 * @author tcurdt
 */
public class ReloadingClassLoader extends ClassLoader implements ReloadNotificationListener {
    
    private final Log log = LogFactory.getLog(ReloadingClassLoader.class);
    
    private final ClassLoader parent;
    private ResourceStore[] stores = new ResourceStore[0];
    private ClassLoader delegate;

    public ReloadingClassLoader( final ClassLoader pParent ) {        
        super(pParent);
        parent = pParent;        

        delegate = new ResourceStoreClassLoader(parent, stores);
    }

    public boolean addResourceStore( final ResourceStore pStore ) {
        try {        
            final int n = stores.length;
            final ResourceStore[] newStores = new ResourceStore[n + 1];
            System.arraycopy(stores, 0, newStores, 1, n);
            newStores[0] = pStore;
            stores = newStores;
            delegate = new ResourceStoreClassLoader(parent, stores);            
            return true;
        } catch ( final RuntimeException e ) {
            log.error("could not add resource store " + pStore);
        }
        return false;
    }

    public boolean removeResourceStore( final ResourceStore pStore ) {

        final int n = stores.length;
        int i = 0;
           
        // FIXME: this should be improved with a Map
        // find the pStore and index position with var i
        while ( ( i < n )  && ( stores[i] != pStore ) ) {
            i++;
        }
                    
        // pStore was not found
        if ( i == n ) {
            return false;
        }
        
        // if stores length > 1 then array copy old values, else create new empty store 
        final ResourceStore[] newStores = new ResourceStore[n - 1];
        if (i > 0) {
            System.arraycopy(stores, 0, newStores, 0, i);
        }
        if (i < n - 1) {
            System.arraycopy(stores, i + 1, newStores, i, (n - i - 1));
        }
            
        stores = newStores;
        delegate = new ResourceStoreClassLoader(parent, stores);
        return true;
    }
    
    public void handleNotification() {
        log.debug("reloading");
        delegate = new ResourceStoreClassLoader(parent, stores);
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
