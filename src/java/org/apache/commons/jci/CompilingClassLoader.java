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
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.eclipse.EclipseJavaCompiler;
import org.apache.commons.jci.listeners.CompilingListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.jci.problems.ConsoleCompilationProblemHandler;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.TransactionalResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tcurdt
 *
 */
public class CompilingClassLoader extends ReloadingClassLoader {
    
    private final static Log log = LogFactory.getLog(CompilingClassLoader.class);
    
    private final TransactionalResourceStore transactionalStore;
    private final JavaCompiler compiler; 
    
    public CompilingClassLoader(final ClassLoader pParent, final File pRepository) {
        this(pParent, pRepository, new TransactionalResourceStore(
                new MemoryResourceStore()) {
                    public void onStart() {
                        };
                    public void onStop() {
                        };
                }
        );
    }
    
    public CompilingClassLoader(final ClassLoader pParent, final File pRepository, final TransactionalResourceStore pStore) {
        this(pParent, pRepository, pStore, new EclipseJavaCompiler());
    }
    
    public CompilingClassLoader(final ClassLoader pParent, final File pRepository, final TransactionalResourceStore pStore, final JavaCompiler pCompiler) {
        super(pParent, pRepository, pStore);

        transactionalStore = pStore;
        compiler = pCompiler;                
    }

    public void start() {
        fam = new FilesystemAlterationMonitor(); 
        fam.addListener(new CompilingListener(
                reader,
                compiler,
                transactionalStore,
                new ConsoleCompilationProblemHandler()
                ) {
            public void reload() {
                super.reload();
                CompilingClassLoader.this.reload();
            }
        }, repository);
        thread = new Thread(fam);         
        thread.start();
    }
}
