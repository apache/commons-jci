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

package org.apache.commons.jci2.compiler.janino;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.jci2.core.compiler.AbstractJavaCompiler;
import org.apache.commons.jci2.core.compiler.CompilationResult;
import org.apache.commons.jci2.core.compiler.JavaCompilerSettings;
import org.apache.commons.jci2.core.problems.CompilationProblem;
import org.apache.commons.jci2.core.readers.ResourceReader;
import org.apache.commons.jci2.core.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.commons.compiler.LocatedException;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.Compiler;
import org.codehaus.janino.FilterWarningHandler;
import org.codehaus.janino.util.StringPattern;
import org.codehaus.janino.util.resource.Resource;
import org.codehaus.janino.util.resource.ResourceCreator;
import org.codehaus.janino.util.resource.ResourceFinder;

/*
 */
public final class JaninoJavaCompiler extends AbstractJavaCompiler {

    private final Log log = LogFactory.getLog(JaninoJavaCompiler.class);

    private final JaninoJavaCompilerSettings defaultSettings;

    public JaninoJavaCompiler() {
    	this(new JaninoJavaCompilerSettings());
    }

    public JaninoJavaCompiler( final JaninoJavaCompilerSettings pSettings ) {
    	defaultSettings = pSettings;
    }

    private final static class JciResource implements Resource {

    	private final String name;
    	private final byte[] bytes;

    	public JciResource( final String pName, final byte[] pBytes ) {
    		name = pName;
    		bytes = pBytes;
    	}

		@Override
        public String getFileName() {
			return name;
		}

		@Override
        public long lastModified() {
			return 0;
		}

		@Override
        public InputStream open() throws IOException {
			return new ByteArrayInputStream(bytes);
		}
    }

    private final static class JciOutputStream extends ByteArrayOutputStream {

    	private final String name;
    	private final ResourceStore store;

    	public JciOutputStream( final String pName, final ResourceStore pStore ) {
    		name = pName;
    		store = pStore;
    	}

		@Override
        public void close() throws IOException {
			super.close();

			final byte[] bytes = toByteArray();

			store.write(name, bytes);
		}
    }

    @Override
    public CompilationResult compile( final String[] pSourceNames, final ResourceReader pResourceReader, final ResourceStore pStore, final ClassLoader pClassLoader, final JavaCompilerSettings pSettings ) {

    	final Collection<CompilationProblem> problems = new ArrayList<>();

    	final StringPattern[] pattern = StringPattern.PATTERNS_NONE;

    	final Compiler compiler = new Compiler(
    			new ResourceFinder() {
					@Override
                    public Resource findResource( final String pSourceName ) {
						final byte[] bytes = pResourceReader.getBytes(pSourceName);

						if (bytes == null) {
							log.debug("failed to find source " + pSourceName);
							return null;
						}

						log.debug("reading " + pSourceName + " (" + bytes.length + ")");

						return new JciResource(pSourceName, bytes);
					}
    			},
    			new ClassLoaderIClassLoader(pClassLoader),
    			new ResourceFinder() {
					@Override
                    public Resource findResource( final String pResourceName ) {
						final byte[] bytes = pStore.read(pResourceName);

						if (bytes == null) {
							log.debug("failed to find " + pResourceName);
							return null;
						}

						log.debug("reading " + pResourceName + " (" + bytes.length + ")");

						return new JciResource(pResourceName, bytes);
					}
    			},
    			new ResourceCreator() {
					@Override
                    public OutputStream createResource( final String pResourceName ) throws IOException {
						return new JciOutputStream(pResourceName, pStore);
					}

					@Override
                    public boolean deleteResource( final String pResourceName ) {
						log.debug("removing " + pResourceName);

						pStore.remove(pResourceName);
						return true;
					}
    			},
    			pSettings.getSourceEncoding(),
    			false,
    			pSettings.isDebug(),
                pSettings.isDebug(),
                pSettings.isDebug(),
    			new FilterWarningHandler(pattern, (pHandle, pMessage, pLocation) -> {
                	final CompilationProblem problem = new JaninoCompilationProblem(pLocation.getFileName(), pLocation, pMessage, false);
                	if (problemHandler != null) {
                		problemHandler.handle(problem);
                	}
                	problems.add(problem);
                })
    			);

    	compiler.setCompileErrorHandler((pMessage, pLocation) -> {
        	final CompilationProblem problem = new JaninoCompilationProblem(pLocation.getFileName(), pLocation, pMessage, true);
        	if (problemHandler != null) {
        		problemHandler.handle(problem);
        	}
        	problems.add(problem);
        });

    	final Resource[] resources = new Resource[pSourceNames.length];
        for (int i = 0; i < pSourceNames.length; i++) {
            log.debug("compiling " + pSourceNames[i]);
            final byte[] source = pResourceReader.getBytes(pSourceNames[i]);
            resources[i] = new JciResource(pSourceNames[i], source);
        }

        try {
            compiler.compile(resources);
        } catch ( final LocatedException e ) {
            problems.add(new JaninoCompilationProblem(e));
        } catch ( final IOException e ) {
            // low level problems reading or writing bytes
        	log.error("this error should have been cought before", e);
        }
        final CompilationProblem[] result = new CompilationProblem[problems.size()];
        problems.toArray(result);
        return new CompilationResult(result);
    }

    @Override
    public JavaCompilerSettings createDefaultSettings() {
        return new JaninoJavaCompilerSettings(defaultSettings);
    }

}
