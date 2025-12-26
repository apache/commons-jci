/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.jci2.fam.monitor;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spawns of a monitoring thread triggering the observers and managing the their listeners.
 */
public final class FilesystemAlterationMonitor implements Runnable {

    private final Log log = LogFactory.getLog(FilesystemAlterationMonitor.class);

    private final Object observersLock = new Object();
    private Map<File, FilesystemAlterationObserver> observers = Collections.unmodifiableMap(new HashMap<File, FilesystemAlterationObserver>());

    /** Delay between calls to {@link FilesystemAlterationObserver#checkAndNotify()}, default 3000 ms */
    private volatile long delay = 3000; // volatile because shared with daemon thread
    private Thread thread;

    private volatile boolean running = true;

    /**
     * Constructs a new instance.
     */
    public FilesystemAlterationMonitor() {
    }

    /**
     * Starts the internal thread.
     */
    public void start() {
        thread = new Thread(this);
        thread.setName("Filesystem Alteration Monitor");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Joins the internal running thread. 
     */
    public void stop() {
        running = false;
        if (thread != null) {
            try {
                thread.join(delay);
            } catch (final InterruptedException e) {
            }
        }
    }

    /**
     * Sets the delay between calls to the observers.
     *
     * @param pDelay the delay in milliseconds (default if not set 3000 ms)
     */
    public void setInterval( final long pDelay ) {
        delay = pDelay;
    }

    /**
     * Adds the given listener for the given file.
     *
     * @param pRoot The file to observe.
     * @param pListener The listener.
     */
    public void addListener( final File pRoot, final FilesystemAlterationListener pListener ) {

        FilesystemAlterationObserver observer;

        synchronized (observersLock) {
            observer = observers.get(pRoot);

            if (observer == null) {
                final Map<File, FilesystemAlterationObserver> newObservers = new HashMap<>(observers);
                observer = new FilesystemAlterationObserverImpl(pRoot);
                newObservers.put(pRoot, observer);
                observers = Collections.unmodifiableMap(newObservers);
            }
        }

        observer.addListener(pListener);
    }

    /**
     * Removes the given listener.
     *
     * @param pListener The listener to remove.
     */
    public void removeListener( final FilesystemAlterationListener pListener ) {
        synchronized (observersLock) {
            for (final FilesystemAlterationObserver observer : observers.values()) {
                observer.removeListener(pListener);
                // FIXME: remove observer if there are no listeners?
            }
        }
    }

    /**
     * Gets the array of listeners for the given file.
     *
     * @param pRoot the file to query.
     * @return the array of listeners for the given file or an empty array.
     */
    public FilesystemAlterationListener[] getListenersFor( final File pRoot  ) {
        final FilesystemAlterationObserver observer = observers.get(pRoot);

        if (observer == null) {
            return new FilesystemAlterationListener[0];
        }

        return observer.getListeners();
    }

    @Override
    public void run() {
        log.debug("fam running");

        while (running) {

            for (final FilesystemAlterationObserver observer : observers.values()) {
                observer.checkAndNotify();
            }

            try {
                Thread.sleep(delay);
            } catch (final InterruptedException e) {
            }
        }

        log.debug("fam exiting");
    }

}
