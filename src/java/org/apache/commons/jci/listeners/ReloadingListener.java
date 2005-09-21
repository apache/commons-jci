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
package org.apache.commons.jci.listeners;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jci.ReloadingClassLoader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ReloadingListener extends AbstractListener{

    private final static Log log = LogFactory.getLog(ReloadingListener.class);

    private final Collection created = new ArrayList();
    private final Collection changed = new ArrayList();
    private final Collection deleted = new ArrayList();

    private final ResourceStore store;
    
    public ReloadingListener(final File pRepository) {
        this(pRepository, new MemoryResourceStore());
    }

    public ReloadingListener(final File pRepository, final ResourceStore pStore) {
        super(pRepository);
        store = pStore;
    }

    public ResourceStore getStore() {
        return store;
    }
    
    public void onStart() {
        created.clear();
        changed.clear();
        deleted.clear();
    }
    public void onStop() {
        boolean reload = false;
        
        log.debug("created:" + created.size()
               + " changed:" + changed.size()
               + " deleted:" + deleted.size());
        
        if (deleted.size() > 0) {
            for (Iterator it = deleted.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                store.remove(ReloadingClassLoader.clazzName(repository, file));
            }
            reload = true;
        }

        if (created.size() > 0) {
            for (Iterator it = created.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                try {
                    final byte[] bytes = IOUtils.toByteArray(new FileReader(file));
                    store.write(ReloadingClassLoader.clazzName(repository, file), bytes);
                } catch(final Exception e) {
                    log.error("could not load " + file, e);
                }
            }
            // FIXME: not necessary
            //reload = true;
        }

        if (changed.size() > 0) {
            for (Iterator it = changed.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                try {
                    final byte[] bytes = IOUtils.toByteArray(new FileReader(file));
                    store.write(ReloadingClassLoader.clazzName(repository, file), bytes);
                } catch(final Exception e) {
                    log.error("could not load " + file, e);
                }
            }
            reload = true;
        }

        needsReload(reload);
    }

    public void onCreateFile( final File file ) {
        // FIXME: for resource reloading remove the filtering
        if (file.getName().endsWith(".class")) {
            created.add(file);
        }
    }
    public void onChangeFile( final File file ) {                
        // FIXME: for resource reloading remove the filtering
        if (file.getName().endsWith(".class")) {
            changed.add(file);
        }
    }
    public void onDeleteFile( final File file ) {
        // FIXME: for resource reloading remove the filtering
        if (file.getName().endsWith(".class")) {
            deleted.add(file);
        }
    }

    public void onCreateDirectory( final File file ) {                
    }
    public void onChangeDirectory( final File file ) {                
    }
    public void onDeleteDirectory( final File file ) {
    }

}
