package org.apache.commons.jci.listeners;

import java.io.File;
import org.apache.commons.jci.stores.ResourceStore;


public abstract class ResourceStoringListener extends NotifyingListener {
    
    public ResourceStoringListener( final File pRepository ) {
        super(pRepository);        
    }
    
    public abstract ResourceStore getStore();
}
