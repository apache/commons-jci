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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.problems.CompilationProblemHandler;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.DebuggingInformation;
import org.codehaus.janino.Descriptor;
import org.codehaus.janino.IClass;
import org.codehaus.janino.IClassLoader;
import org.codehaus.janino.Java;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.UnitCompiler;
import org.codehaus.janino.Scanner.LocatedException;
import org.codehaus.janino.util.ClassFile;

/**
 *
 * @author art@gramlich-net.com
 */
public class JaninoJavaCompiler implements JavaCompiler {

    private final static Log log = LogFactory.getLog(JaninoJavaCompiler.class);

    private static class CompilingIClassLoader extends IClassLoader {

        private final Map types = new HashMap();
        private final ResourceReader resourceReader;
        private final CompilationProblemHandler problemHandler;
        private final Map classes;

        private CompilingIClassLoader(ResourceReader resourceReader, CompilationProblemHandler problemHandler, Map classes) {
            super(new ClassLoaderIClassLoader());
            this.resourceReader = resourceReader;
            this.problemHandler = problemHandler;
            this.classes = classes;
            super.postConstruct();
        }

        protected IClass findIClass(final String type) {
            final String className = Descriptor.toClassName(type);
            if (types.containsKey(type)) {
                return (IClass) types.get(type);
            }
            final String fileNameForClass = className.replace('.', File.separatorChar) + ".java";

            final char[] content = resourceReader.getContent(fileNameForClass);
            if (content == null) {
                return null;
            }
            final ByteArrayInputStream instream = new ByteArrayInputStream(new String(content).getBytes());
            Scanner scanner = null;
            try {
                scanner = new Scanner(fileNameForClass, instream, "UTF-8");
                final Java.CompilationUnit unit = new Parser(scanner).parseCompilationUnit();
                final UnitCompiler uc = new UnitCompiler(unit, this);
                log.debug("compile " + className);
                final ClassFile[] classFiles = uc.compileUnit(DebuggingInformation.ALL);
                for (int i = 0; i < classFiles.length; i++) {
                    log.debug("compiled " + classFiles[i].getThisClassName());
                    classes.put(classFiles[i].getThisClassName(), classFiles[i].toByteArray());
                }
                final IClass ic = uc.findClass(className);
                if (null != ic) {
                    types.put(type, ic);
                }
                return ic;
            } catch (final LocatedException e) {
                problemHandler.handle(new JaninoCompilationProblem(e));
            } catch (final IOException e) {
                problemHandler.handle(new JaninoCompilationProblem(fileNameForClass, "IO:" + e.getMessage(), true));
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

    public void compile(final String[] classes, final ResourceReader in,
            final ResourceStore store, final CompilationProblemHandler problemHandler) {
        final Map classFilesByName = new HashMap();
        final IClassLoader icl = new CompilingIClassLoader(in, problemHandler, classFilesByName);
        for (int i = 0; i < classes.length; i++) {
            log.debug("compiling " + classes[i]);
            icl.loadIClass(Descriptor.fromClassName(classes[i]));
        }
        // Store all fully compiled classes
        for (Iterator i=classFilesByName.keySet().iterator(); i.hasNext();) {
            final String name = (String)i.next();
            final byte[] bytes = (byte[]) classFilesByName.get(name);
            store.write(name,bytes);
        }
    }
}