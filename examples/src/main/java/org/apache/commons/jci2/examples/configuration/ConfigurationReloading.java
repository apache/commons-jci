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

package org.apache.commons.jci2.examples.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.jci2.listeners.FileChangeListener;
import org.apache.commons.jci2.monitor.FilesystemAlterationListener;
import org.apache.commons.jci2.monitor.FilesystemAlterationMonitor;
import org.apache.commons.jci2.monitor.FilesystemAlterationObserver;

/**
 * 
 * @author tcurdt
 */
public final class ConfigurationReloading {

    private final FilesystemAlterationMonitor fam = new FilesystemAlterationMonitor();

    private void run(final String[] args) {

        final File configFile = new File("some.properties");

        System.out.println("Watching " + configFile.getAbsolutePath());

        final Collection<Configurable> configurables = new ArrayList<Configurable>();

        final FilesystemAlterationListener listener = new FileChangeListener() {
            @Override
            public void onStop(final FilesystemAlterationObserver pObserver) {
                super.onStop(pObserver);

                if (hasChanged()) {
                    System.out.println("Configuration change detected " + configFile);

                    final Properties props = new Properties();
                    InputStream is = null; 
                    try {
                    	is = new FileInputStream(configFile);
                        props.load(is);

                        System.out.println("Notifying about configuration change " + configFile);

                        for (final Configurable configurable : configurables) {
                            configurable.configure(props);
                        }

                    } catch (final Exception e) {
                        System.err.println("Failed to load configuration " + configFile);
                    } finally {
                    	try {
							is.close();
						} catch (final IOException e) {
						}
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
            } catch (final InterruptedException e) {
            }
        }
    }

    public static void main(final String[] args) {
        new ConfigurationReloading().run(args);
    }
}
