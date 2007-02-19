/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jci.monitor;

import java.io.File;

/**
 * FilesystemAlterationObserver represents the state of files
 * below a certain root directory. It implements the code to
 * check the filesystem and notify listeners.
 * 
 * @author tcurdt
 */
public interface FilesystemAlterationObserver {

	// FilesystemAlterationObserver( final File pRootDirectory );
	
	File getRootDirectory();
	
	
	void checkAndNotify();

	
	void addListener( final FilesystemAlterationListener pListener );	

	void removeListener( final FilesystemAlterationListener pListener );
	
	FilesystemAlterationListener[] getListeners();
}