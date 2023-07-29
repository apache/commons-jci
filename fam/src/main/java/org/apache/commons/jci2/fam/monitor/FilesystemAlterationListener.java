/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.jci2.fam.monitor;

import java.io.File;

/**
 * A listener that receives events of filesystem modifications.
 * The observer basically represents the source of the events.
 * The file root and its state. (see FilesystemAlterationObserver)
 *
 * @author tcurdt
 */
public interface FilesystemAlterationListener {

    void onStart( final FilesystemAlterationObserver pObserver );
    void onFileCreate( final File pFile );
    void onFileChange( final File pFile );
    void onFileDelete( final File pFile );
    void onDirectoryCreate( final File pDir );
    void onDirectoryChange( final File pDir );
    void onDirectoryDelete( final File pDir );
    void onStop( final FilesystemAlterationObserver pObserver );
}
