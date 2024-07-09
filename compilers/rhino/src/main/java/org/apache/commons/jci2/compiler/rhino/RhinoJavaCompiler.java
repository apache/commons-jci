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

package org.apache.commons.jci2.compiler.rhino;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.jci2.core.compiler.AbstractJavaCompiler;
import org.apache.commons.jci2.core.compiler.CompilationResult;
import org.apache.commons.jci2.core.compiler.JavaCompilerSettings;
import org.apache.commons.jci2.core.problems.CompilationProblem;
import org.apache.commons.jci2.core.readers.ResourceReader;
import org.apache.commons.jci2.core.stores.ResourceStore;
import org.apache.commons.jci2.core.utils.ConversionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.GeneratedClassLoader;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.optimizer.ClassCompiler;

/**
 * @author tcurdt
 */
public final class RhinoJavaCompiler extends AbstractJavaCompiler {

    private final Log log = LogFactory.getLog(RhinoJavaCompiler.class);

    private final JavaCompilerSettings defaultSettings;

    public RhinoJavaCompiler() {
        defaultSettings = new RhinoJavaCompilerSettings();
    }

    /**
     * based on code from dev.helma.org
     * https://dev.helma.org/source/file/helma/branches/rhinoloader/src/org/helma/javascript/RhinoLoader.java/?revision=95
     */
    private final class RhinoCompilingClassLoader extends ClassLoader {

        private final ScriptableObject scope;
        private final ResourceReader reader;
        private final ResourceStore store;

        private final Collection<CompilationProblem> problems = new ArrayList<>();

        private final class ProblemCollector implements ErrorReporter {

            @Override
            public void error(final String pMessage, final String pFileName, final int pLine, final String pScript, final int pColumn) {

                final CompilationProblem problem = new RhinoCompilationProblem(pMessage, pFileName, pLine, pScript, pColumn, true);

                if (problemHandler != null) {
                    problemHandler.handle(problem);
                }

                problems.add(problem);
            }

            @Override
            public void warning(final String pMessage, final String pFileName, final int pLine, final String pScript, final int pColumn) {

                final CompilationProblem problem = new RhinoCompilationProblem(pMessage, pFileName, pLine, pScript, pColumn, false);

                if (problemHandler != null) {
                    problemHandler.handle(problem);
                }

                problems.add(problem);
            }

            @Override
            public EvaluatorException runtimeError(final String pMessage, final String pFileName, final int pLine, final String pScript, final int pColumn) {
                return new EvaluatorException(pMessage, pFileName, pLine, pScript, pColumn);
            }
        }

        public RhinoCompilingClassLoader( final ResourceReader pReader, final ResourceStore pStore, final ClassLoader pClassLoader) {
            super(pClassLoader);

            reader = pReader;
            store = pStore;

            final Context context = Context.enter();
            scope = new ImporterTopLevel(context);
            Context.exit();
        }

        public Collection<CompilationProblem> getProblems() {
            return problems;
        }

        @Override
        protected Class<?> findClass( final String pName ) throws ClassNotFoundException {
            final Context context = Context.enter();
            context.setErrorReporter(new ProblemCollector());

            try {
                return compileClass(context, pName);
            } catch (final EvaluatorException | IOException e) {
                throw new ClassNotFoundException(e.getMessage(), e);
            } finally {
                Context.exit();
            }
        }

        private Class<?> compileClass( final Context pContext, final String pClassName) throws IOException, ClassNotFoundException {

            Class<?> superclass = null;

            final String pSourceName = pClassName.replace('.', '/') + ".js";

            final Scriptable target = evaluate(pContext, pSourceName);

            final Object baseClassName = ScriptableObject.getProperty(target, "__extends__");

            if (baseClassName instanceof String) {
                superclass = Class.forName((String) baseClassName);
            }

            final List<Class<?>> interfaceClasses = new ArrayList<>();

            final Object interfaceNames = ScriptableObject.getProperty(target, "__implements__");

            if (interfaceNames instanceof NativeArray) {

                final NativeArray interfaceNameArray = (NativeArray) interfaceNames;

                for (int i=0; i<interfaceNameArray.getLength(); i++) {

                    final Object obj = interfaceNameArray.get(i, interfaceNameArray);

                    if (obj instanceof String) {
                        interfaceClasses.add(Class.forName((String) obj));
                    }
                }

            } else if (interfaceNames instanceof String) {

                interfaceClasses.add(Class.forName((String) interfaceNames));

            }

            final Class<?>[] interfaces;

            if (!interfaceClasses.isEmpty()) {
                interfaces = new Class[interfaceClasses.size()];
                interfaceClasses.toArray(interfaces);
            } else {
                // FIXME: hm ...really no empty array good enough?
                interfaces = null;
            }

            return compileClass(pContext, pSourceName, pClassName, superclass, interfaces);

        }

        private Class<?> compileClass( final Context pContext, final String pSourceName, final String pClassName, final Class<?> pSuperClass, final Class<?>[] pInterfaces) {

            final CompilerEnvirons environments = new CompilerEnvirons();
            environments.initFromContext(pContext);
            final ClassCompiler compiler = new ClassCompiler(environments);

            if (pSuperClass != null) {
                compiler.setTargetExtends(pSuperClass);
            }

            if (pInterfaces != null) {
                compiler.setTargetImplements(pInterfaces);
            }

            final byte[] sourceBytes = reader.getBytes(pSourceName);

            final Object[] classes = compiler.compileToClassFiles(new String(sourceBytes), getName(pSourceName), 1, pClassName);

            final GeneratedClassLoader loader = pContext.createClassLoader(pContext.getApplicationClassLoader());

            Class<?> clazz = null;

            for (int i = 0; i < classes.length; i += 2) {

                final String clazzName = (String) classes[i];
                final byte[] clazzBytes = (byte[]) classes[i+1];

                store.write(clazzName.replace('.', '/') + ".class", clazzBytes);

                final Class<?> c = loader.defineClass(clazzName, clazzBytes);
                loader.linkClass(c);

                if (i == 0) {
                    clazz = c;
                }

            }

            return clazz;
        }

        private String getName(final String s) {
            final int i = s.lastIndexOf('/');
            if (i < 0) {
                return s;
            }

            return s.substring(i + 1);
        }

        private Scriptable evaluate( final Context pContext, final String pSourceName) throws JavaScriptException, IOException {

            if (!reader.isAvailable(pSourceName)) {
                throw new FileNotFoundException("File " + pSourceName + " not found");
            }

            final Scriptable target = pContext.newObject(scope);

            final byte[] sourceBytes = reader.getBytes(pSourceName);

            final Reader reader = new InputStreamReader(new ByteArrayInputStream(sourceBytes));

            pContext.evaluateReader(target, reader, getName(pSourceName), 1, null);

            return target;
        }

    }

    @Override
    public CompilationResult compile( final String[] pResourcePaths, final ResourceReader pReader, final ResourceStore pStore, final ClassLoader pClassLoader, final JavaCompilerSettings pSettings ) {

        final RhinoCompilingClassLoader cl = new RhinoCompilingClassLoader(pReader, pStore, pClassLoader);

        for (final String pResourcePath : pResourcePaths) {
            log.debug("compiling " + pResourcePath);

            final String clazzName = ConversionUtils.convertResourceToClassName(pResourcePath);
            try {
                cl.loadClass(clazzName);
            } catch (final ClassNotFoundException e) {
            }
        }

        final Collection<CompilationProblem> problems = cl.getProblems();
        final CompilationProblem[] result = new CompilationProblem[problems.size()];
        problems.toArray(result);
        return new CompilationResult(result);
    }

    @Override
    public JavaCompilerSettings createDefaultSettings() {
        return defaultSettings;
    }

}
