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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * It's a runnable that spawns of a monitoring thread triggering the
 * the observers and managing the their listeners.
 * 
 * @author tcurdt
 */
public final class FilesystemAlterationMonitor implements Runnable {

    private final Log log = LogFactory.getLog(FilesystemAlterationMonitor.class);

    public static class UniqueMultiValueMap extends MultiHashMap {

		private static final long serialVersionUID = 1L;

		public UniqueMultiValueMap() {
            super( );
        }

        public UniqueMultiValueMap(Map copy) {
            super( copy );
        }

        protected Collection createCollection( Collection copy ) {
            if (copy != null) {
                return new HashSet(copy);
            }
            return new HashSet();
        }
        
    }
    
    private long delay = 3000;
    private volatile boolean running = true;
    private Thread thread;
    private Map observers = new HashMap(); 
        
    public FilesystemAlterationMonitor() {
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


    public void addListener( final File pRoot, final FilesystemAlterationListener pListener ) {
    	
    	FilesystemAlterationObserver observer;
    	
    	synchronized (observers) {
        	observer = (FilesystemAlterationObserver)observers.get(pRoot);

        	if (observer == null) {
        		observer = new FilesystemAlterationObserverImpl(pRoot);
        		observers.put(pRoot, observer);
        	}			
		}
    	
    	observer.addListener(pListener);    	
    }

//    public void removeListeners( final File pRoot ) {
//    	FilesystemAlterationObserver observer;
//    	
//    	synchronized (observers) {
//        	observer = (FilesystemAlterationObserver)observers.get(pRoot);
//
//        	if (observer == null) {
//        		return;
//        	}			
//		}
//    	
//    	final FilesystemAlterationListener[] listeners = observer.getListeners();
//    	for (int i = 0; i < listeners.length; i++) {
//			final  FilesystemAlterationListener listener = listeners[i];
//	    	observer.removeListener(listener);    				
//		}
//    }
    
    public void removeListener( final FilesystemAlterationListener pListener ) {
    	synchronized (observers) {
    		for (Iterator it = observers.values().iterator(); it.hasNext();) {
				final FilesystemAlterationObserver observer = (FilesystemAlterationObserver) it.next();
				observer.removeListener(pListener);
			}
    	}
    }
    
//    public FilesystemAlterationListener[] getListeners() {
//    	final Collection listeners = new ArrayList();
//    	
//    	synchronized (observers) {
//    		for (Iterator it = observers.values().iterator(); it.hasNext();) {
//				final FilesystemAlterationObserver observer = (FilesystemAlterationObserver) it.next();
//			}
//    	}
//    	return null;
//    }

    public FilesystemAlterationListener[] getListenersFor( final File pRoot  ) {
    	FilesystemAlterationObserver observer;
    	
    	synchronized (observers) {
        	observer = (FilesystemAlterationObserver)observers.get(pRoot);

        	if (observer == null) {
        		return new FilesystemAlterationListener[0];
        	}			
		}

    	return observer.getListeners();
    }


    public void run() {
        log.debug("fam running");
        
        while (true) {
            if (!running) {
                break;
            }

            final FilesystemAlterationObserver[] observerArray;
            
        	synchronized (observers) {
        		observerArray = (FilesystemAlterationObserver[])observers.values().toArray(
        				new FilesystemAlterationObserver[observers.size()]); 

        	}        	
            
            for (int i = 0; i < observerArray.length; i++) {
				final FilesystemAlterationObserver observer = observerArray[i];
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
