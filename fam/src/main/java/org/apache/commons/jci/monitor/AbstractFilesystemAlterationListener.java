package org.apache.commons.jci.monitor;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractFilesystemAlterationListener implements FilesystemAlterationListener {

    private final Log log = LogFactory.getLog(AbstractFilesystemAlterationListener.class);

    private final static class Signal {
        public boolean triggered;
    }

    private final Signal eventSignal = new Signal();
    private final Signal checkSignal = new Signal();

	private int createdFiles;
	private int createdDirectories;
	private int changedFiles;
	private int changedDirectories;
	private int deletedFiles;
	private int deletedDirectories;
    
	
	
	
    public void onChangeDirectory( final File pDir ) {
    	changedDirectories++;
	}

	public void onChangeFile( final File pFile ) {
		changedFiles++;
	}

	public void onCreateDirectory( final File pDir ) {
		createdDirectories++;
	}

	public void onCreateFile( final File pFile) {
		createdFiles++;
	}

	public void onDeleteDirectory( final File pDir ) {
		deletedDirectories++;
	}

	public void onDeleteFile( final File pfile ) {
		deletedFiles++;
	}

	public void onStart() {
    	createdFiles = 0;
    	createdDirectories = 0;
    	changedFiles = 0;
    	changedDirectories = 0;
    	deletedFiles = 0;
    	deletedDirectories = 0;
    }
    
    public void onStop() {
    	if (createdFiles > 0 || createdDirectories > 0 ||
    	    changedFiles > 0 || changedDirectories > 0 ||
    	    deletedFiles > 0 || deletedDirectories >0) {

    		synchronized(eventSignal) {
                eventSignal.triggered = true;
                eventSignal.notifyAll();
            }    	    		
    	}
    	
        synchronized(checkSignal) {
            checkSignal.triggered = true;
            checkSignal.notifyAll();
        }    	
    }
    
    
	public int getChangedDirectories() {
		return changedDirectories;
	}

	public int getChangedFiles() {
		return changedFiles;
	}

	public int getCreatedDirectories() {
		return createdDirectories;
	}

	public int getCreatedFiles() {
		return createdFiles;
	}

	public int getDeletedDirectories() {
		return deletedDirectories;
	}

	public int getDeletedFiles() {
		return deletedFiles;
	}

	public void waitForEvent() throws Exception {
        synchronized(eventSignal) {
            eventSignal.triggered = false;
        }
        log.debug("waiting for change");
        if (!waitForSignal(eventSignal, 10)) {
            throw new Exception("timeout");
        }
    }
    
    /*
     * we don't reset the signal
     * so if there was a check it is
     * already true and exit immediatly
     * otherwise it will behave just
     * like waitForCheck()
     */
    public void waitForFirstCheck() throws Exception {
        log.debug("waiting for first check");
        if (!waitForSignal(checkSignal, 10)) {
            throw new Exception("timeout");
        }        
    }

    public void waitForCheck() throws Exception {
        synchronized(checkSignal) {
            checkSignal.triggered = false;
        }
        log.debug("waiting for check");
        if (!waitForSignal(checkSignal, 10)) {
            throw new Exception("timeout");
        }
    }
    
    private boolean waitForSignal(final Signal pSignal, final int pSecondsTimeout) {
        int i = 0;
        while(true) {
            synchronized(pSignal) {
                if (!pSignal.triggered) {
                    try {
                        pSignal.wait(1000);
                    } catch (InterruptedException e) {
                    }

                    if (++i > pSecondsTimeout) {
                        log.error("timeout after " + pSecondsTimeout + "s");
                        return false;
                    }
                    
                } else {
                    pSignal.triggered = false;
                    break;
                }
            }
        }        
        return true;
    }

}
