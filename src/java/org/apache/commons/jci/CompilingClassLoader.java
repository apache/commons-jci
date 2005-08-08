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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.eclipse.EclipseJavaCompiler;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.jci.problems.ConsoleCompilationProblemHandler;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.TransactionalResourceStore;

/**
 * @author tcurdt
 *
 */
public class CompilingClassLoader extends ClassLoader {
    
    private final static Log log = LogFactory.getLog(CompilingClassLoader.class);
    
    private final ClassLoader parent;
    private final File repository;
    private ClassLoader delegate;
    private final TransactionalResourceStore store;
    private final ResourceReader reader;
    private final JavaCompiler compiler; 
    private final FilesystemAlterationMonitor fam;

    public CompilingClassLoader(final ClassLoader pParent, final File pRepository) {
        this(pParent, pRepository, new EclipseJavaCompiler());
    }    
 
    public CompilingClassLoader(final ClassLoader pParent, final File pRepository, final JavaCompiler compiler) {
        this(pParent, pRepository,
                new TransactionalResourceStore(new MemoryResourceStore()) {
                    public void onStart() {
                        };
                    public void onStop() {
                        };
                },
                compiler
        );
    }
    
    public CompilingClassLoader(final ClassLoader pParent, final File pRepository, final TransactionalResourceStore pStore) {
        this(pParent, pRepository, pStore, new EclipseJavaCompiler());
    }
    
    public CompilingClassLoader(final ClassLoader pParent, final File pRepository, final TransactionalResourceStore pStore,
            final JavaCompiler compiler) {
        super(pParent);
        parent = pParent;        
        repository = pRepository;
        
        reader = new FileResourceReader(repository);
        store = pStore;
        this.compiler = compiler;
                
        fam = new FilesystemAlterationMonitor(); 

        fam.addListener(new FilesystemAlterationListener() {

            private Collection created = new ArrayList();
            private Collection changed = new ArrayList();
            private Collection deleted = new ArrayList();
            
            public void onStart() {
                created.clear();
                changed.clear();
                deleted.clear();
                store.onStart();
            }
            public void onStop() {
                /*
                log.debug("resources " +
                        created.size() + " created, " + 
                        changed.size() + " changed, " + 
                        deleted.size() + " deleted");
                        */

                boolean reload = false;
                
                if (deleted.size() > 0) {
                    for (Iterator it = deleted.iterator(); it.hasNext();) {
                        final File file = (File) it.next();
                        store.remove(clazzName(repository, file));
                    }
                    reload = true;
                }
                                
                final Collection compileables = new ArrayList();
                compileables.addAll(created);
                compileables.addAll(changed);

                final String[] clazzes = new String[compileables.size()];
                
                if (compileables.size() > 0) {
                    
                    int i = 0;
                    for (Iterator it = compileables.iterator(); it.hasNext();) {
                        final File file = (File) it.next();
                        clazzes[i] = clazzName(repository,file);
                        //log.debug(clazzes[i]);
                        i++;
                    }
                    
                    final ConsoleCompilationProblemHandler problemHandler = new ConsoleCompilationProblemHandler();

                    compiler.compile(
                            clazzes,
                            reader,
                            store,
                            problemHandler
                            );
                    
                    
                    log.debug(
                            problemHandler.getErrorCount() + " errors, " +
                            problemHandler.getWarningCount() + " warnings"
                            );
                
                    if (problemHandler.getErrorCount() > 0) {
                        for (int j = 0; j < clazzes.length; j++) {
                            store.remove(clazzes[j]);
                        }
                    }
                    
                    reload = true;                    

                }

                store.onStop();

                //log.debug(store);
                
                if (reload) {
                    reload();
                }                
            }

            public void onCreateFile( final File file ) {
                if (file.getName().endsWith(".java")) {
                    created.add(file);
                }
            }
            public void onChangeFile( final File file ) {                
                if (file.getName().endsWith(".java")) {
                    changed.add(file);
                }
            }
            public void onDeleteFile( final File file ) {
                if (file.getName().endsWith(".java")) {
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
