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
package org.apache.commons.jci.monitor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.eclipse.EclipseJavaCompiler;
import org.apache.commons.jci.problems.ConsoleCompilationProblemHandler;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;


/**
 * @author tcurdt
 *
 */
public final class Test {

    private final static Log log = LogFactory.getLog(Test.class);

    public static String clazzName( final File base, final File file ) {
        final int rootLength = base.getAbsolutePath().length();
        final String absFileName = file.getAbsolutePath();
        final String relFileName = absFileName.substring(
                rootLength + 1,
                absFileName.length() - ".java".length()
                );
        final String clazzName = relFileName.replace('/','.');
        return clazzName;
    }
    
    public static void main(String[] args) {
        final File repository = new File("/home/tcurdt/dev/jci/classes");
        
        final ResourceReader reader = new FileResourceReader(repository);
        final ResourceStore store = new MemoryResourceStore();
        final JavaCompiler compiler = new EclipseJavaCompiler();
        
        
        final AlterationMonitor fam = new AlterationMonitor(repository); 

        fam.addListener(new AlterationListener() {

            private Collection created = new ArrayList();
            private Collection changed = new ArrayList();
            private Collection deleted = new ArrayList();
            
            public void onStart() {
                created.clear();
                changed.clear();
                deleted.clear();
            }
            public void onStop() {
                log.debug(created.size() + " resources created");
                log.debug(changed.size() + " resources changed");
                log.debug(deleted.size() + " resources deleted");

                if (deleted.size() > 0) {
	                for (Iterator it = deleted.iterator(); it.hasNext();) {
	                    final File file = (File) it.next();
	                    store.remove(clazzName(fam.getRoot(), file));
	                }
                }
                                
                final Collection compileables = new ArrayList();
                compileables.addAll(created);
                compileables.addAll(changed);
                
                if (compileables.size() > 0) {
	                
	                final String[] clazzes = new String[compileables.size()];
	                int i = 0;
	                for (Iterator it = compileables.iterator(); it.hasNext();) {
	                    final File file = (File) it.next();
	                    clazzes[i] = clazzName(fam.getRoot(),file);
	                    log.debug(clazzes[i]);
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
                }
            }
            public void onCreateFile( final File file ) {
                created.add(file);
            }
            public void onChangeFile( final File file ) {                
                changed.add(file);
            }
            public void onDeleteFile( final File file ) {
                deleted.add(file);
            }

            public void onCreateDirectory( final File file ) {                
            }
            public void onChangeDirectory( final File file ) {                
            }
            public void onDeleteDirectory( final File file ) {
            }
            });
        
        Thread myThread = new Thread (fam); 
        myThread.start();
                
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
        }
        
    }
}
