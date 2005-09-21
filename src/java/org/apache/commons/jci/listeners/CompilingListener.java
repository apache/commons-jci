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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.jci.ReloadingClassLoader;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.eclipse.EclipseJavaCompiler;
import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.stores.TransactionalResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CompilingListener extends AbstractListener {

    private final static Log log = LogFactory.getLog(CompilingListener.class);
    
    private final Collection created = new ArrayList();
    private final Collection changed = new ArrayList();
    private final Collection deleted = new ArrayList();
    
    private final JavaCompiler compiler;
    private final ResourceReader reader;
    private final TransactionalResourceStore transactionalStore;
    private CompilationResult lastResult;
    
    public CompilingListener( final File pRepository ) {
        this(pRepository,
             new EclipseJavaCompiler(),
             new TransactionalResourceStore(new MemoryResourceStore())
             );
    }
    
    public CompilingListener(
            final File pRepository,
            final JavaCompiler pCompiler,
            final TransactionalResourceStore pTransactionalStore
            ) {
        super(pRepository);
        compiler = pCompiler;
        transactionalStore = pTransactionalStore;

        reader = new FileResourceReader(pRepository);
        lastResult = null;
    }
    
    public ResourceStore getStore() {
        return transactionalStore;
    }

    public synchronized CompilationResult getCompilationResult() {
        return lastResult;
    }
    
    public void onStart() {
        created.clear();
        changed.clear();
        deleted.clear();
        transactionalStore.onStart();
    }
    public void onStop() {
        log.debug(
                created.size() + " created, " + 
                changed.size() + " changed, " + 
                deleted.size() + " deleted");

        boolean reload = false;
        
        if (deleted.size() > 0) {
            for (Iterator it = deleted.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                transactionalStore.remove(ReloadingClassLoader.clazzName(repository, file));
            }
            reload = true;
        }
                        
        final Collection compileables = new ArrayList();
        // FIXME: only compile ".java" resources to support resource reloading
        compileables.addAll(created);
        compileables.addAll(changed);

        final String[] clazzes = new String[compileables.size()];
        
        if (compileables.size() > 0) {
            
            int i = 0;
            for (Iterator it = compileables.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                clazzes[i] = ReloadingClassLoader.clazzName(repository, file);
                i++;
            }
            
            final CompilationResult result =
                compiler.compile(
                    clazzes,
                    reader,
                    transactionalStore
                    );
            
            synchronized(this) {
                lastResult = result;
            }
            
            final CompilationProblem[] errors = result.getErrors();
            final CompilationProblem[] warnings = result.getWarnings();
            
            log.debug(
                    errors.length + " errors, " +
                    warnings.length + " warnings"
                    );
        
            if (errors.length > 0) {
                // FIXME: they need to be marked for re-compilation
                for (int j = 0; j < clazzes.length; j++) {
                    transactionalStore.remove(clazzes[j]);
                }
            }
            
            reload = true;
        }

        transactionalStore.onStop();

        needsReload(reload);
    }

    public void onCreateFile( final File file ) {
        // FIXME: to support resource reloading do the suffix filtering in onStop
        if (file.getName().endsWith(".java")) {
            created.add(file);
        }
    }
    public void onChangeFile( final File file ) {                
        // FIXME: to support resource reloading do the suffix filtering in onStop
        if (file.getName().endsWith(".java")) {
            changed.add(file);
        }
    }
    public void onDeleteFile( final File file ) {
        // FIXME: to support resource reloading do the suffix filtering in onStop
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
}
