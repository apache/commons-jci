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
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.jci2.core.compiler.CompilationResult;
import org.apache.commons.jci2.core.compiler.JavaCompiler;
import org.apache.commons.jci2.core.compiler.JavaCompilerFactory;
import org.apache.commons.jci2.core.problems.CompilationProblem;
import org.apache.commons.jci2.core.readers.FileResourceReader;
import org.apache.commons.jci2.core.readers.ResourceReader;
import org.apache.commons.jci2.core.stores.MemoryResourceStore;
import org.apache.commons.jci2.core.stores.ResourceStore;
import org.apache.commons.jci2.core.stores.TransactionalResourceStore;
import org.apache.commons.jci2.core.utils.ConversionUtils;
import org.apache.commons.jci2.fam.monitor.FilesystemAlterationObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A CompilingListener is an improved version of the ReloadingListener.
 * It even compiles the classes from source before doing the reloading.
 *
 * @author tcurdt
 */
public class CompilingListener extends ReloadingListener {

    private final Log log = LogFactory.getLog(CompilingListener.class);

    private final JavaCompiler compiler;
    private final TransactionalResourceStore transactionalStore;
    private ResourceReader reader;
    private CompilationResult lastResult;

    public CompilingListener() {
        this(new JavaCompilerFactory().createCompiler("eclipse"));
    }

    public CompilingListener( final JavaCompiler pCompiler ) {
        this(pCompiler, new TransactionalResourceStore(new MemoryResourceStore()));
    }

    public CompilingListener( final JavaCompiler pCompiler, final TransactionalResourceStore pTransactionalStore ) {
        super(pTransactionalStore);
        compiler = pCompiler;
        transactionalStore = pTransactionalStore;
        lastResult = null;
    }

    public JavaCompiler getCompiler() {
        return compiler;
    }

    public String getSourceFileExtension() {
        return ".java";
    }

    public ResourceReader getReader( final FilesystemAlterationObserver pObserver ) {
        return new FileResourceReader(pObserver.getRootDirectory());
    }

    public String getSourceNameFromFile( final FilesystemAlterationObserver pObserver, final File pFile ) {
        return ConversionUtils.stripExtension(ConversionUtils.getResourceNameFromFileName(ConversionUtils.relative(pObserver.getRootDirectory(), pFile))) + getSourceFileExtension();
    }

    @Override
    public ResourceStore getStore() {
        return transactionalStore;
    }

    public synchronized CompilationResult getCompilationResult() {
        return lastResult;
    }

    @Override
    public void onStart( final FilesystemAlterationObserver pObserver ) {
        super.onStart(pObserver);

        reader = getReader(pObserver);

        transactionalStore.onStart();
    }

    public String[] getResourcesToCompile( final FilesystemAlterationObserver pObserver ) {
        final Collection<File> created = getCreatedFiles();
        final Collection<File> changed = getChangedFiles();

        final Collection<String> resourceNames = new ArrayList<>();

        for (final File createdFile : created) {
            if (createdFile.getName().endsWith(getSourceFileExtension())) {
                resourceNames.add(getSourceNameFromFile(pObserver, createdFile));
            }
        }

        for (final File changedFile : changed) {
            if (changedFile.getName().endsWith(getSourceFileExtension())) {
                resourceNames.add(getSourceNameFromFile(pObserver, changedFile));
            }
        }

        final String[] result = new String[resourceNames.size()];
        resourceNames.toArray(result);
        return result;
    }

    @Override
    public boolean isReloadRequired( final FilesystemAlterationObserver pObserver ) {
        boolean reload = false;

        final Collection<File> created = getCreatedFiles();
        final Collection<File> changed = getChangedFiles();
        final Collection<File> deleted = getDeletedFiles();

        log.debug("created:" + created.size() + " changed:" + changed.size() + " deleted:" + deleted.size() + " resources");

        if (!deleted.isEmpty()) {
            for (final File deletedFile : deleted) {
                final String resourceName = ConversionUtils.getResourceNameFromFileName(ConversionUtils.relative(pObserver.getRootDirectory(), deletedFile));

                if (resourceName.endsWith(getSourceFileExtension())) {
                    // if source resource got removed delete the corresponding class
                    transactionalStore.remove(ConversionUtils.stripExtension(resourceName) + ".class");
                } else {
                    // ordinary resource to be removed
                    transactionalStore.remove(resourceName);
                }

                // FIXME: does not remove nested classes

            }
            reload = true;
        }

        final String[] resourcesToCompile = getResourcesToCompile(pObserver);

        if (resourcesToCompile.length > 0) {

            log.debug(resourcesToCompile.length + " classes to compile");

            final CompilationResult result = compiler.compile(resourcesToCompile, reader, transactionalStore);

            synchronized(this) {
                lastResult = result;
            }

            final CompilationProblem[] errors = result.getErrors();
            final CompilationProblem[] warnings = result.getWarnings();

            log.debug(errors.length + " errors, " + warnings.length + " warnings");

            if (errors.length > 0) {
                // FIXME: they need to be marked for re-compilation
                // and then added as compileables again
                for (final String element : resourcesToCompile) {
                    transactionalStore.remove(element);
                }
            }

            reload = true;
        }

        return reload;
    }
}
