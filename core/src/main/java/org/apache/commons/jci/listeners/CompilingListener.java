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
import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.stores.TransactionalResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CompilingListener extends ReloadingListener {

    private final static Log log = LogFactory.getLog(CompilingListener.class);
    
    private final JavaCompiler compiler;
    private final ResourceReader reader;
    private final TransactionalResourceStore transactionalStore;
    private CompilationResult lastResult;
    
    public CompilingListener( final File pRepository ) {
        this(pRepository,
             JavaCompilerFactory.getInstance().createCompiler("eclipse"),
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
        super.onStart();
        transactionalStore.onStart();
    }

    public void onStop() {
        boolean reload = false;

        log.debug("created:" + created.size()
                + " changed:" + changed.size()
                + " deleted:" + deleted.size()
                + " resources");

        
        if (deleted.size() > 0) {
            for (Iterator it = deleted.iterator(); it.hasNext();) {
                final File file = (File) it.next();
                // FIXME: does not remove nested classes
                transactionalStore.remove(ReloadingClassLoader.clazzName(repository, file));
            }
            reload = true;
        }
                        
        final Collection compileables = new ArrayList();
        
        for (final Iterator it = created.iterator(); it.hasNext();) {
            final File createdFile = (File) it.next();
            if (createdFile.getName().endsWith(".java")) {
                compileables.add(createdFile);
            }
        }
        
        for (final Iterator it = changed.iterator(); it.hasNext();) {
            final File changedFile = (File) it.next();
            if (changedFile.getName().endsWith(".java")) {
                compileables.add(changedFile);
            }
        }

        if (compileables.size() > 0) {

            log.debug(compileables.size()
                    + " classes to compile"
                    );

            int i = 0;
            final String[] clazzes = new String[compileables.size()];            
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
                // and then added as compileables again
                for (int j = 0; j < clazzes.length; j++) {
                    transactionalStore.remove(clazzes[j]);
                }
            }
            
            reload = true;
        }

        transactionalStore.onStop();

        checked(reload);
    }
}
