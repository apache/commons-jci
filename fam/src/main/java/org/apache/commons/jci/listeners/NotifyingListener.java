package org.apache.commons.jci.listeners;

import java.io.File;


public abstract class NotifyingListener extends AbstractFilesystemAlterationListener {

    public final static class Signal {
        public boolean triggered;
    }

    protected final File repository;
    protected NotificationListener notificationListener;

    public NotifyingListener(final File pRepository) {
        repository = pRepository;        
    }
    
    public File getRepository() {
        return repository;
    }

    public void setNotificationListener(final NotificationListener pNotificationListener) {
        notificationListener = pNotificationListener;
    }

  
}
