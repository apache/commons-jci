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
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tcurdt
 *
 */
public class ReloadingClassLoader extends ClassLoader {
    
    private final static Log log = LogFactory.getLog(ReloadingClassLoader.class);
    
    private final ClassLoader parent;
    private final File repository;
    private ClassLoader delegate;
    private final ResourceStore store;
    private final ResourceReader reader;
    private final FilesystemAlterationMonitor fam;

    public ReloadingClassLoader(final ClassLoader pParent, final File pRepository) {
        this(pParent, pRepository, new MemoryResourceStore());
    }
    
    public ReloadingClassLoader(final ClassLoader pParent, final File pRepository, final ResourceStore pStore) {
        super(pParent);
        parent = pParent;        
        repository = pRepository;        
        reader = new FileResourceReader(repository);
        store = pStore;
                
        fam = new FilesystemAlterationMonitor(); 

        fam.addListener(new FilesystemAlterationListener() {

            private Collection created = new ArrayList();
            private Collection changed = new ArrayList();
            private Collection deleted = new ArrayList();
            
            public void onStart() {
                created.clear();
                changed.clear();
                deleted.clear();
            }
            public void onStop() {

                boolean reload = false;
                
                if (deleted.size() > 0) {
                    for (Iterator it = deleted.iterator(); it.hasNext();) {
                        final File file = (File) it.next();
                        store.remove(clazzName(repository, file));
                    }
                    reload = true;
                }

                if (created.size() > 0) {
                    for (Iterator it = created.iterator(); it.hasNext();) {
                        final File file = (File) it.next();
                        try {
                            final byte[] bytes = IOUtils.toByteArray(new FileReader(file));
                            store.write(clazzName(repository, file), bytes);
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
            }, repository);
        
        delegate = new ResourceStoreClassLoader(parent, store);

        Thread myThread = new Thread(fam); 
        myThread.start();        
    }

    private void reload() {
        log.debug("reloading");
        delegate = new ResourceStoreClassLoader(parent, store );
        
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
