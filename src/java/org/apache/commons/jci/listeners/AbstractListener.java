package org.apache.commons.jci.listeners;

import java.io.File;
import org.apache.commons.jci.ReloadingClassLoader;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.stores.ResourceStore;


public abstract class AbstractListener implements FilesystemAlterationListener {

    protected final File repository;
    protected ReloadingClassLoader reloader;

    public AbstractListener(final File pRepository) {
        repository = pRepository;        
    }

    public void setReloadingClassLoader(final ReloadingClassLoader pReloader) {
        reloader = pReloader;
    }
    
    public abstract ResourceStore getStore();
    
    public File getRepository() {
        return repository;
    }

    protected void needsReload( final boolean pReload ) {
        reloader.reload(pReload);
    }
}
