/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jci.compilers.janino;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.jci.compilers.AbstractJavaCompiler;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.problems.CompilationProblemHandler;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.CompileException;
import org.codehaus.janino.DebuggingInformation;
import org.codehaus.janino.Descriptor;
import org.codehaus.janino.IClass;
import org.codehaus.janino.IClassLoader;
import org.codehaus.janino.Java;
import org.codehaus.janino.Location;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.UnitCompiler;
import org.codehaus.janino.WarningHandler;
import org.codehaus.janino.Scanner.LocatedException;
import org.codehaus.janino.UnitCompiler.ErrorHandler;
import org.codehaus.janino.util.ClassFile;

/**
 * @author art@gramlich-net.com
 */
public final class JaninoJavaCompiler extends AbstractJavaCompiler {

    private final static Log log = LogFactory.getLog(JaninoJavaCompiler.class);

    private static class CompilingIClassLoader extends IClassLoader {

        private final Map types = new HashMap();
        private final ResourceReader resourceReader;
        private final Map classes;
        private final Collection problems = new ArrayList();

        private CompilingIClassLoader(final ResourceReader pResourceReader, final Map pClasses) {
            super(new ClassLoaderIClassLoader());
            resourceReader = pResourceReader;
            classes = pClasses;
            super.postConstruct();
        }

        protected Collection getProblems() {
            return problems;
        }
        
        protected IClass findIClass(final String pType) {
            final String className = Descriptor.toClassName(pType);
            if (types.containsKey(pType)) {
                return (IClass) types.get(pType);
            }
            final String fileNameForClass = className.replace('.', File.separatorChar) + ".java";

            final char[] content = resourceReader.getContent(fileNameForClass);
            if (content == null) {
                return null;
            }
            final Reader reader = new BufferedReader(new CharArrayReader(content));
            Scanner scanner = null;
            try {
                scanner = new Scanner(fileNameForClass, reader);
                final Java.CompilationUnit unit = new Parser(scanner).parseCompilationUnit();
                final UnitCompiler uc = new UnitCompiler(unit, this);
                uc.setCompileErrorHandler(new ErrorHandler() {
                    public void handleError(final String pMessage, final Location pOptionalLocation) throws CompileException {
                        problems.add(new JaninoCompilationProblem(pOptionalLocation, pMessage, true));
                    }
                });
                uc.setWarningHandler(new WarningHandler() {
                    public void handleWarning(final String pHandle, final String pMessage, final Location pOptionalLocation) {
                        problems.add(new JaninoCompilationProblem(pOptionalLocation, pMessage, false));
                    }
                });
                log.debug("compile " + className);
                final ClassFile[] classFiles = uc.compileUnit(DebuggingInformation.ALL);
                for (int i = 0; i < classFiles.length; i++) {
                    log.debug("compiled " + classFiles[i].getThisClassName());
                    classes.put(classFiles[i].getThisClassName(), classFiles[i].toByteArray());
                }
                final IClass ic = uc.findClass(className);
                if (null != ic) {
                    types.put(pType, ic);
                }
                return ic;
            } catch (final LocatedException e) {
                problems.add(new JaninoCompilationProblem(e));
            } catch (final IOException e) {
                problems.add(new JaninoCompilationProblem(fileNameForClass, "IO:" + e.getMessage(), true));
            } finally {
                if (scanner != null) {
                    try {
                        scanner.close();
                    } catch (IOException e) {
                        log.error("IOException occured while compiling " + className, e);
                    }
                }
            }
            return null;
        }
    }

    public CompilationResult compile(
            final String[] pClasses,
            final ResourceReader pResourceReader,
            final ResourceStore pStore
            ) {
        final Map classFilesByName = new HashMap();
        final CompilingIClassLoader icl = new CompilingIClassLoader(pResourceReader, classFilesByName);
        for (int i = 0; i < pClasses.length; i++) {
            log.debug("compiling " + pClasses[i]);
            icl.loadIClass(Descriptor.fromClassName(pClasses[i]));
        }
        // Store all fully compiled classes
        for (Iterator i = classFilesByName.entrySet().iterator(); i.hasNext();) {
            final Map.Entry entry = (Map.Entry)i.next();
            pStore.write((String)entry.getKey(), (byte[])entry.getValue());
        }
        
        return new CompilationResult(icl.getProblems());
    }

    private static final class CompilationProblemHandlerAdapter implements ErrorHandler, WarningHandler {
        private final CompilationProblemHandler problemHandler;

        public CompilationProblemHandlerAdapter(final CompilationProblemHandler pProblemHandler) {
            problemHandler = pProblemHandler;
        }

        public void handleError(final String pMessage, final Location pOptionalLocation) throws CompileException {
            problemHandler.handle(new JaninoCompilationProblem(pOptionalLocation, pMessage, true));
        }

        public void handleWarning(final String pHandle, final String pMessage, final Location pOptionalLocation) {
            problemHandler.handle(new JaninoCompilationProblem(pOptionalLocation, pMessage, false));
        }

    }

}