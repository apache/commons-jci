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

    private class CompilingIClassLoader extends IClassLoader {

        private ResourceReader resourceReader;
        private CompilationProblemHandler problemHandler;
        private Map classes,types;
        private ClassLoaderIClassLoader loader;

        private CompilingIClassLoader(ResourceReader resourceReader, CompilationProblemHandler problemHandler, Map classes) {
            super(null);
            this.resourceReader = resourceReader;
            this.problemHandler = problemHandler;
            this.classes = classes;
            this.types = new HashMap();
            this.loader = new ClassLoaderIClassLoader();
            super.postConstruct();
        }

        protected IClass findIClass(String type) {
            String className = Descriptor.toClassName(type);
            if (className.startsWith("java.") ||
                    className.startsWith("javax.") ||
                    className.startsWith("sun.") ||
                    className.startsWith("org.xml.") ||
                    className.startsWith("org.w3c.")
                    ) {
                    //Quickly hand these off
                    return loader.loadIClass(type);
            }
            if (types.containsKey(type)) {
                return (IClass) types.get(type);
            }
            String fileNameForClass = className.replace('.', File.separatorChar) + ".java";
            if (!resourceReader.isAvailable(fileNameForClass)) {
                return loader.loadIClass(type);
            }

            ByteArrayInputStream instream = new ByteArrayInputStream(new String(resourceReader.getContent(fileNameForClass)).getBytes());
            Scanner scanner = null;
            try {
                scanner = new Scanner(fileNameForClass, instream, "UTF-8");
                Java.CompilationUnit unit = new Parser(scanner).parseCompilationUnit();
                UnitCompiler uc = new UnitCompiler(unit, loader);
                log.debug("compile " + className);
                ClassFile[] classFiles = uc.compileUnit(DebuggingInformation.ALL);
                for (int i = 0; i < classFiles.length; i++) {
                    log.debug("compiled " + classFiles[i].getThisClassName());
                    classes.put(classFiles[i].getThisClassName(), classFiles[i].toByteArray());
                }
                IClass ic = uc.findClass(className);
                if (null != ic) {
                    types.put(type, ic);
                }
                return ic;
            } catch (LocatedException e) {
                problemHandler.handle(new JaninoCompilationProblem(e));
            } catch (IOException e) {
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

    public void compile(String[] classes, ResourceReader in,
            ResourceStore store, CompilationProblemHandler problemHandler) {
        Map classFilesByName = new HashMap();
        IClassLoader icl = new CompilingIClassLoader(in, problemHandler, classFilesByName);
        for (int i = 0; i < classes.length; i++) {
            log.debug("compiling " + classes[i]);
            icl.loadIClass(Descriptor.fromClassName(classes[i]));
        }
        // Store all fully compiled classes
        for (Iterator i=classFilesByName.keySet().iterator(); i.hasNext();) {
            String name = (String)i.next();
            byte[] bytes = (byte[]) classFilesByName.get(name);
            store.write(name,bytes);
        }
    }
}