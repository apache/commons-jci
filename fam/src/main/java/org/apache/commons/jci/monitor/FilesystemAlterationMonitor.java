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
package org.apache.commons.jci.monitor;

import java.io.File;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tcurdt
 */
public final class FilesystemAlterationMonitor implements Runnable {

    private final Log log = LogFactory.getLog(FilesystemAlterationMonitor.class);

    private final FilesystemAlterationObserver observer;

    private long delay = 3000;
    private volatile boolean running = true;
    private Thread thread;

    public FilesystemAlterationMonitor() {
    	observer = new FilesystemAlterationObserverImpl();
    }


    public void start() {
        thread = new Thread(this);
        thread.start();
    }


    public void stop() {
        running = false;

        try {
            thread.join(delay);
        } catch (InterruptedException e) {
        }
    }


    public void setInterval( final long pDelay ) {
        delay = pDelay;
    }


    public void addListener( final FilesystemAlterationListener pListener ) {
    	observer.addListener( pListener );
    }

    public Collection getListeners() {
        return observer.getListeners();
    }

    public Collection getListenersFor( final File pRepository ) {
        return observer.getListenersFor( pRepository );
    }

    public void removeListener( final FilesystemAlterationListener listener ) {
    	observer.removeListener( listener );
    }


    public void run() {
        log.debug("fam running");
        while (true) {
            if (!running) {
                break;
            }

            observer.check();

            try {
                Thread.sleep(delay);
            } catch (final InterruptedException e) {
            }
        }
        log.debug("fam exiting");
    }



    public String toString() {
        return observer.toString();
    }   
}
