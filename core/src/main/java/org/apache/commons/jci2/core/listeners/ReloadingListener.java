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

package org.apache.commons.jci2.core.listeners;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jci2.core.ReloadingClassLoader;
import org.apache.commons.jci2.core.stores.MemoryResourceStore;
import org.apache.commons.jci2.core.stores.ResourceStore;
import org.apache.commons.jci2.core.stores.Transactional;
import org.apache.commons.jci2.core.utils.ConversionUtils;
import org.apache.commons.jci2.fam.listeners.AbstractFilesystemAlterationListener;
import org.apache.commons.jci2.fam.monitor.FilesystemAlterationObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Listener waits for FAM events to trigger a reload of classes
 * or resources.
 *
 * @author tcurdt
 */
public class ReloadingListener extends AbstractFilesystemAlterationListener {

    private final Log log = LogFactory.getLog(ReloadingListener.class);

    private final Set<ReloadNotificationListener> notificationListeners = new HashSet<>();
    private final ResourceStore store;

    public ReloadingListener() {
        this(new MemoryResourceStore());
    }

    public ReloadingListener( final ResourceStore pStore ) {
        store = pStore;
    }

    public ResourceStore getStore() {
        return store;
    }

    public void addReloadNotificationListener( final ReloadNotificationListener pNotificationListener ) {
        notificationListeners.add(pNotificationListener);

        if (pNotificationListener instanceof ReloadingClassLoader) {
            ((ReloadingClassLoader)pNotificationListener).addResourceStore(store);
        }

    }

    public boolean isReloadRequired( final FilesystemAlterationObserver pObserver ) {
        boolean reload = false;

        final Collection<File> created = getCreatedFiles();
        final Collection<File> changed = getChangedFiles();
        final Collection<File> deleted = getDeletedFiles();

        log.debug("created:" + created.size() + " changed:" + changed.size() + " deleted:" + deleted.size() + " resources");

        if (!deleted.isEmpty()) {
            for (final File file : deleted) {
                final String resourceName = ConversionUtils.getResourceNameFromFileName(ConversionUtils.relative(pObserver.getRootDirectory(), file));
                store.remove(resourceName);
            }
            reload = true;
        }

        if (!created.isEmpty()) {
            for (final File file : created) {
                FileInputStream is = null;
                try {
                    is = new FileInputStream(file);
                    final byte[] bytes = IOUtils.toByteArray(is);
                    final String resourceName = ConversionUtils.getResourceNameFromFileName(ConversionUtils.relative(pObserver.getRootDirectory(), file));
                    store.write(resourceName, bytes);
                } catch (final Exception e) {
                    log.error("could not load " + file, e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }

        if (!changed.isEmpty()) {
            for (final File file : changed) {
                FileInputStream is = null;
                try {
                    is = new FileInputStream(file);
                    final byte[] bytes = IOUtils.toByteArray(is);
                    final String resourceName = ConversionUtils.getResourceNameFromFileName(ConversionUtils.relative(pObserver.getRootDirectory(), file));
                    store.write(resourceName, bytes);
                } catch (final Exception e) {
                    log.error("could not load " + file, e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
            reload = true;
        }

        return reload;
    }

    @Override
    public void onStop( final FilesystemAlterationObserver pObserver ) {

        if (store instanceof Transactional) {
            ((Transactional)store).onStart();
        }

        final boolean reload = isReloadRequired(pObserver);

        if (store instanceof Transactional) {
            ((Transactional)store).onStop();
        }

        if (reload) {
            notifyReloadNotificationListeners();
        }

        super.onStop(pObserver);
    }

    void notifyReloadNotificationListeners() {
        for (final ReloadNotificationListener listener : notificationListeners) {
            log.debug("notifying listener " + listener);

            listener.handleNotification();
        }
    }

    @Override
    public void onDirectoryCreate( final File pDir ) {
    }
    @Override
    public void onDirectoryChange( final File pDir ) {
    }
    @Override
    public void onDirectoryDelete( final File pDir ) {
    }
}
