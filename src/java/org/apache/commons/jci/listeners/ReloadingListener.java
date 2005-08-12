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
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ReloadingListener implements FilesystemAlterationListener{

    private final static Log log = LogFactory.getLog(ReloadingListener.class);

    private final Collection created = new ArrayList();
    private final Collection changed = new ArrayList();
    private final Collection deleted = new ArrayList();

    private final ResourceStore store;
    
    public ReloadingListener(final ResourceStore pStore) {
        store = pStore;        
    }
    
    public void onStart(final File repository) {
        created.clear();
        changed.clear();
        deleted.clear();
    }
    public void onStop(final File pRepository) {

        boolean reload = false;
        
        if (deleted.size() > 0) {
            for (Iterator it = deleted.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                store.remove(ReloadingClassLoader.clazzName(pRepository, file));
            }
            reload = true;
        }

        if (created.size() > 0) {
            for (Iterator it = created.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                try {
                    final byte[] bytes = IOUtils.toByteArray(new FileReader(file));
                    store.write(ReloadingClassLoader.clazzName(pRepository, file), bytes);
                } catch(final Exception e) {
                    log.error("could not load " + file, e);
                }
            }
        }

        if (changed.size() > 0) {
            reload = true;
        }

        if (reload) {
            reload();
        }                
    }

    public void onCreateFile( final File file ) {
        if (file.getName().endsWith(".class")) {
            created.add(file);
        }
    }
    public void onChangeFile( final File file ) {                
        if (file.getName().endsWith(".class")) {
            changed.add(file);
        }
    }
    public void onDeleteFile( final File file ) {
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

    public void reload() {        
    }
}
