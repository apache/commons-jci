package org.apache.commons.jci.listeners;

import java.io.File;


public class FileChangeListener extends NotifyingListener {

    private boolean changed;
        
    public FileChangeListener(final File pRepository) {
        super(pRepository);
    }
    
    public void onStart() {
        changed = false;
    }

    public void onStop() {
    	super.onStop();
    }

    
    public void onChangeFile( File pFile ) {
        changed = true;
    }


    public void onCreateFile( File pFile ) {
        changed = true;
    }


    public void onDeleteFile( File pFile ) {
        changed = true;
    }


    public void onChangeDirectory( File pDir ) {
    }

    public void onCreateDirectory( File pDir ) {
    }

    public void onDeleteDirectory( File pDir ) {
    }
    
}
