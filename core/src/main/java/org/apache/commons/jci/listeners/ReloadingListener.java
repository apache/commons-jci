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
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jci.ReloadingClassLoader;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.stores.Transactional;
import org.apache.commons.jci.utils.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ReloadingListener extends AbstractFilesystemAlterationListener {

    private final Log log = LogFactory.getLog(ReloadingListener.class);
    
    private final Set notificationListeners = new HashSet();
    private final ResourceStore store;
    
    public ReloadingListener() {
        this(new MemoryResourceStore());
    }

    public ReloadingListener( final ResourceStore pStore ) {
        store = pStore;
    }
    
    public ResourceStore getStore() {
        return store;
    }

    public void addReloadNotificationListener( final ReloadNotificationListener pNotificationListener ) {
    	notificationListeners.add(pNotificationListener);
    	
    	if (pNotificationListener instanceof ReloadingClassLoader) {
    		((ReloadingClassLoader)pNotificationListener).addResourceStore(store);
    	}
    	
    }
    
    public String getResourceNameFromRelativeFileName( final String pRelativeFileName ) {
    	if ('/' == File.separatorChar) {
    		return pRelativeFileName;
    	}
    	
    	return pRelativeFileName.replace(File.separatorChar, '/');
    }
    
    public boolean isReloadRequired( final FilesystemAlterationObserver pObserver ) {
    	boolean reload = false;
    	
        final Collection created = getCreatedFiles();
        final Collection changed = getChangedFiles();
        final Collection deleted = getDeletedFiles();
        
        log.debug("created:" + created.size() + " changed:" + changed.size() + " deleted:" + deleted.size() + " resources");

        if (deleted.size() > 0) {
            for (Iterator it = deleted.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                final String resourceName = getResourceNameFromRelativeFileName(ClassUtils.relative(pObserver.getRootDirectory(), file));
                store.remove(resourceName);
            }
            reload = true;
        }

        if (created.size() > 0) {
            for (Iterator it = created.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                FileInputStream is = null;
                try {
                	is = new FileInputStream(file);
                    final byte[] bytes = IOUtils.toByteArray(is);
                    final String resourceName = getResourceNameFromRelativeFileName(ClassUtils.relative(pObserver.getRootDirectory(), file));
                    store.write(resourceName, bytes);
                } catch(final Exception e) {
                    log.error("could not load " + file, e);
                } finally {
                	IOUtils.closeQuietly(is);
                }
            }
        }

        if (changed.size() > 0) {
            for (Iterator it = changed.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                FileInputStream is = null;
                try {
                	is = new FileInputStream(file);
                    final byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
                    final String resourceName = getResourceNameFromRelativeFileName(ClassUtils.relative(pObserver.getRootDirectory(), file));
                    store.write(resourceName, bytes);
                } catch(final Exception e) {
                    log.error("could not load " + file, e);
                } finally {
                	IOUtils.closeQuietly(is);
                }
            }
            reload = true;
        }
    	
        return reload;
    }
    
    public void onStop( final FilesystemAlterationObserver pObserver ) {
        
        
        if (store instanceof Transactional) {
            ((Transactional)store).onStart();
        }

        final boolean reload = isReloadRequired(pObserver);

        if (store instanceof Transactional) {
            ((Transactional)store).onStop();
        }
        
        if (reload) {
        	notifyReloadNotificationListeners();
        }
        
        super.onStop(pObserver);
    }

    void notifyReloadNotificationListeners() {
    	for (Iterator it = notificationListeners.iterator(); it.hasNext();) {
    		final ReloadNotificationListener listener = (ReloadNotificationListener) it.next();
			listener.handleNotification();
		}    	
    }
    
    public void onDirectoryCreate( final File pDir ) {                
    }
    public void onDirectoryChange( final File pDir ) {                
    }
    public void onDirectoryDelete( final File pDir ) {
    }
}
