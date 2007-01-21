package org.apache.commons.jci.monitor;

import java.io.File;
import java.util.Collection;

public interface FilesystemAlterationObserver {

	void check();
	
	
	// FIXME: the listener stuff should probably not part of this interface
	
	void addListener( final FilesystemAlterationListener pListener );

	Collection getListeners();

	Collection getListenersFor( final File pRepository );

	void removeListener( final FilesystemAlterationListener listener );

}