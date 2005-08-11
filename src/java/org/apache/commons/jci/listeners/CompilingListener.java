package org.apache.commons.jci.listeners;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.jci.ReloadingClassLoader;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.problems.ConsoleCompilationProblemHandler;
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
    
    public CompilingListener(
            final ResourceReader pReader,
            final JavaCompiler pCompiler,
            final TransactionalResourceStore pTransactionalStore
            ) {
        compiler = pCompiler;
        reader = pReader;
        transactionalStore = pTransactionalStore;
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
            
            final ConsoleCompilationProblemHandler problemHandler = new ConsoleCompilationProblemHandler();

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
