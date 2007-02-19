package org.apache.commons.jci.listeners;

import java.io.File;

import org.apache.commons.jci.monitor.FilesystemAlterationObserver;


public class FileChangeListener extends AbstractFilesystemAlterationListener {

    private boolean changed;
    
    public boolean hasChanged() {
    	return changed;
    }
    
    public void onStart( final FilesystemAlterationObserver pObserver ) {
        changed = false;
    	super.onStart(pObserver);
    }

    public void onStop( final FilesystemAlterationObserver pObserver ) {
    	super.onStop(pObserver);
    }

    
    public void onFileChange( final File pFile ) {
        changed = true;
    }


    public void onFileCreate( final File pFile ) {
        changed = true;
    }


    public void onFileDelete( final File pFile ) {
        changed = true;
    }


    public void onDirectoryChange( final File pDir ) {
    }

    public void onDirectoryCreate( final File pDir ) {
    }

    public void onDirectoryDelete( final File pDir ) {
    }
    
}
