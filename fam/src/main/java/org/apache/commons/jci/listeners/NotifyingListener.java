package org.apache.commons.jci.listeners;

import java.io.File;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class NotifyingListener implements FilesystemAlterationListener {

    private final Log log = LogFactory.getLog(NotifyingListener.class);

    public final static class Signal {
        public boolean triggered;
    }

    protected final File repository;
    protected NotificationListener notificationListener;
    private final Signal notificationSignal = new Signal();
    private final Signal checkSignal = new Signal();

    public NotifyingListener(final File pRepository) {
        repository = pRepository;        
    }
    
    public File getRepository() {
        return repository;
    }

    public void setNotificationListener(final NotificationListener pNotificationListener) {
        notificationListener = pNotificationListener;
    }

    protected void checked( final boolean pNotify ) {
        if (pNotify) {
            if (notificationListener != null) {
                notificationListener.handleNotification();
            }
            synchronized(notificationSignal) {
                notificationSignal.triggered = true;
                notificationSignal.notifyAll();
            }
        }
        synchronized(checkSignal) {
            checkSignal.triggered = true;
            checkSignal.notifyAll();
        }
    }
    
    
    public void waitForNotification() throws Exception {
        synchronized(notificationSignal) {
            notificationSignal.triggered = false;
        }
        log.debug("waiting for reload signal");
        if (!waitForSignal(notificationSignal, 10)) {
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
        log.debug("waiting for first signal");
        if (!waitForSignal(checkSignal, 10)) {
            throw new Exception("timeout");
        }        
    }

    public void waitForCheck() throws Exception {
        synchronized(checkSignal) {
            checkSignal.triggered = false;
        }
        log.debug("waiting for check signal");
        if (!waitForSignal(checkSignal, 10)) {
            throw new Exception("timeout");
        }
    }
    
    private boolean waitForSignal(final Signal pSignal, final int pSecondsTimeout) {
        int i = 0;
        while(true) {
            synchronized(pSignal) {
                //log.debug("loop");
                if (!pSignal.triggered) {
                    try {
                        //log.debug("waiting");
                        pSignal.wait(1000);
                    } catch (InterruptedException e) {
                        ;
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
        
        log.debug("caught signal");
        return true;
    }
  
}
