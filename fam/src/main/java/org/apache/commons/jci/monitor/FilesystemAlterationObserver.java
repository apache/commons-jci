package org.apache.commons.jci.monitor;

import java.io.File;
import java.util.Collection;

public interface FilesystemAlterationObserver {

	public abstract void addListener(
			final FilesystemAlterationListener pListener);

	public abstract Collection getListeners();

	public abstract Collection getListenersFor(final File pRepository);

	public abstract void removeListener(
			final FilesystemAlterationListener listener);

	public abstract void check();

}