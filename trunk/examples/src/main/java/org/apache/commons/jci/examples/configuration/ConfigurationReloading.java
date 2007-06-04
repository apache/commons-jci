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

package org.apache.commons.jci.examples.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.jci.listeners.FileChangeListener;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;

/**
 * 
 * @author tcurdt
 */
public final class ConfigurationReloading {

    private final FilesystemAlterationMonitor fam = new FilesystemAlterationMonitor();

    private void run(String[] args) {

        final File configFile = new File("some.properties");

        System.out.println("Watching " + configFile.getAbsolutePath());

        final Collection configurables = new ArrayList();

        final FilesystemAlterationListener listener = new FileChangeListener() {
            public void onStop(FilesystemAlterationObserver pObserver) {
                super.onStop(pObserver);

                if (hasChanged()) {
                    System.out.println("Configuration change detected " + configFile);

                    final Properties props = new Properties();
                    try {

                        props.load(new FileInputStream(configFile));

                        System.out.println("Notifying about configuration change " + configFile);

                        for (Iterator it = configurables.iterator(); it.hasNext();) {
                            final Configurable configurable = (Configurable) it.next();
                            configurable.configure(props);
                        }

                    } catch (Exception e) {
                        System.err.println("Failed to load configuration " + configFile);
                    }

                }
            }
        };

        fam.addListener(configFile, listener);
		fam.start();

		configurables.add(new Something());

        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public static void main(String[] args) {
        new ConfigurationReloading().run(args);
    }
}
