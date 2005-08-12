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
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.problems.CompilationProblemHandler;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.TransactionalResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CompilingListener implements FilesystemAlterationListener {

    private final static Log log = LogFactory.getLog(CompilingListener.class);
    
    private final Collection created = new ArrayList();
    private final Collection changed = new ArrayList();
    private final Collection deleted = new ArrayList();
    
    private final JavaCompiler compiler;
    private final ResourceReader reader;
    private final TransactionalResourceStore transactionalStore;
    private final CompilationProblemHandler problemHandler;
    
    public CompilingListener(
            final ResourceReader pReader,
            final JavaCompiler pCompiler,
            final TransactionalResourceStore pTransactionalStore,
            final CompilationProblemHandler pProblemHandler
            ) {
        compiler = pCompiler;
        reader = pReader;
        transactionalStore = pTransactionalStore;
        problemHandler = pProblemHandler;
    }
    
    public void onStart(final File pRepository) {
        created.clear();
        changed.clear();
        deleted.clear();
        transactionalStore.onStart();
    }
    public void onStop(final File pRepository) {
        log.debug("resources " +
                created.size() + " created, " + 
                changed.size() + " changed, " + 
                deleted.size() + " deleted");

        boolean reload = false;
        
        if (deleted.size() > 0) {
            for (Iterator it = deleted.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                transactionalStore.remove(ReloadingClassLoader.clazzName(pRepository, file));
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
                clazzes[i] = ReloadingClassLoader.clazzName(pRepository,file);
                //log.debug(clazzes[i]);
                i++;
            }
            
            compiler.compile(
                    clazzes,
                    reader,
                    transactionalStore,
                    problemHandler
                    );
            
            
            log.debug(
                    problemHandler.getErrorCount() + " errors, " +
                    problemHandler.getWarningCount() + " warnings"
                    );
        
            if (problemHandler.getErrorCount() > 0) {
                for (int j = 0; j < clazzes.length; j++) {
                    transactionalStore.remove(clazzes[j]);
                }
            }
            
            reload = true;                    

        }

        transactionalStore.onStop();

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

    protected void reload() {
    }
}
