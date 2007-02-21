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
package org.apache.commons.jci.compilers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.utils.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

public final class EclipseJavaCompiler extends AbstractJavaCompiler {

    private final Log log = LogFactory.getLog(EclipseJavaCompiler.class);
    private final Map settings;

    public EclipseJavaCompiler() {
        this(new EclipseJavaCompilerSettings());
    }

    public EclipseJavaCompiler(final Map pSettings) {
        settings = pSettings;
    }

    public EclipseJavaCompiler(final EclipseJavaCompilerSettings pSettings) {
        settings = pSettings.getMap();
    }

    final class CompilationUnit implements ICompilationUnit {

        final private String clazzName;
        final private String fileName;
        final private char[] typeName;
        final private char[][] packageName;
        final private ResourceReader reader;

        CompilationUnit( final ResourceReader pReader, final String pSourceFile ) {
            reader = pReader;
            clazzName = ClassUtils.convertResourceToClassName(pSourceFile);
            fileName = pSourceFile;
            int dot = clazzName.lastIndexOf('.');
            if (dot > 0) {
                typeName = clazzName.substring(dot + 1).toCharArray();
            } else {
                typeName = clazzName.toCharArray();
            }
            
            log.debug("className=" + clazzName);
            log.debug("fileName=" + fileName);
            log.debug("typeName=" + new String(typeName)); 
            
            final StringTokenizer izer = new StringTokenizer(clazzName, ".");
            packageName = new char[izer.countTokens() - 1][];
            for (int i = 0; i < packageName.length; i++) {
                packageName[i] = izer.nextToken().toCharArray();
                log.debug("package[" + i + "]=" + new String(packageName[i]));
            }
        }

        public char[] getFileName() {
            return fileName.toCharArray();
        }

        public char[] getContents() {
            return new String(reader.getBytes(fileName)).toCharArray();
        }

        public char[] getMainTypeName() {
            return typeName;
        }

        public char[][] getPackageName() {
            return packageName;
        }
    }

    public org.apache.commons.jci.compilers.CompilationResult compile(
            final String[] pSourceFiles,
            final ResourceReader pReader,
            final ResourceStore pStore,
            final ClassLoader pClassLoader
            ) {

        final Map settingsMap = settings;
//        final Set sourceFileIndex = new HashSet();
        final ICompilationUnit[] compilationUnits = new ICompilationUnit[pSourceFiles.length];
        for (int i = 0; i < compilationUnits.length; i++) {
            final String sourceFile = pSourceFiles[i];
            compilationUnits[i] = new CompilationUnit(pReader, sourceFile);
//            sourceFileIndex.add(sourceFile);
            log.debug("compiling " + sourceFile);
        }

        final IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.proceedWithAllProblems();
        final IProblemFactory problemFactory = new DefaultProblemFactory(Locale.getDefault());
        final INameEnvironment nameEnvironment = new INameEnvironment() {

            public NameEnvironmentAnswer findType( final char[][] compoundTypeName ) {
                final StringBuffer result = new StringBuffer();
                for (int i = 0; i < compoundTypeName.length; i++) {
                    if (i != 0) {
                        result.append('.');
                    }
                    result.append(compoundTypeName[i]);
                }

                //log.debug("finding compoundTypeName=" + result.toString());

            	return findType(result.toString());
            }

            public NameEnvironmentAnswer findType( final char[] typeName, final char[][] packageName ) {
                final StringBuffer result = new StringBuffer();
                for (int i = 0; i < packageName.length; i++) {
                    result.append(packageName[i]);
                    result.append('.');
                }
                
//            	log.debug("finding typeName=" + new String(typeName) + " packageName=" + result.toString());

            	result.append(typeName);
                return findType(result.toString());
            }

            private NameEnvironmentAnswer findType( final String clazzName ) {
            	
            	if (isPackage(clazzName)) {
            		return null;
            	}
            	
            	log.debug("finding " + clazzName);
            	
            	final String resourceName = ClassUtils.convertClassToResourcePath(clazzName);
            	
                final byte[] clazzBytes = pStore.read(clazzName);
                if (clazzBytes != null) {
                    log.debug("loading from store " + clazzName);

                    final char[] fileName = clazzName.toCharArray();
                    try {
                        final ClassFileReader classFileReader = new ClassFileReader(clazzBytes, fileName, true);
                        return new NameEnvironmentAnswer(classFileReader, null);
                    } catch (final ClassFormatException e) {
                        log.error("wrong class format", e);
                        return null;
                    }
                }
                
            	log.debug("not in store " + clazzName);
            	
//                if (pReader.isAvailable(clazzName.replace('.', '/') + ".java")) {
//                    log.debug("compile " + clazzName);
//                    ICompilationUnit compilationUnit = new CompilationUnit(pReader, clazzName);
//                    return new NameEnvironmentAnswer(compilationUnit, null);
//                }

                final InputStream is = pClassLoader.getResourceAsStream(resourceName);
                if (is == null) {
                	log.debug("class " + clazzName + " not found");
                	return null;
                }

                final byte[] buffer = new byte[8192];
                final ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
                int count;
                try {
                    while ((count = is.read(buffer, 0, buffer.length)) > 0) {
                        baos.write(buffer, 0, count);
                    }
                    baos.flush();
                    final char[] fileName = clazzName.toCharArray();
                    final ClassFileReader classFileReader = new ClassFileReader(baos.toByteArray(), fileName, true);
                    return new NameEnvironmentAnswer(classFileReader, null);
                } catch (final IOException e) {
                    log.error("could not read class", e);
                    return null;
                } catch (final ClassFormatException e) {
                    log.error("wrong class format", e);
                    return null;
                } finally {
                    try {
                        baos.close();
                    } catch (final IOException oe) {
                        log.error("could not close output stream", oe);
                    }
                    try {
                        is.close();
                    } catch (final IOException ie) {
                        log.error("could not close input stream", ie);
                    }
                }
            }

            private boolean isPackage( final String clazzName ) {
            	
            	log.debug("isPackage? " + clazzName);
//            	if ("jci.Simple".equals(clazzName)) {
//            		log.debug("no");
//            		return false;
//            	}
//
//            	if ("jci.String".equals(clazzName)) {
//            		log.debug("no");
//            		return false;
//            	}
//
//            	if ("java.lang.Object".equals(clazzName)) {
//            		log.debug("no");
//            		return false;
//            	}
//
//            	if ("java.lang.String".equals(clazzName)) {
//            		log.debug("no");
//            		return false;
//            	}

            	if ("java".equals(clazzName)) {
            		log.debug("yes");
            		return true;
            	}
            	if ("java.lang".equals(clazzName)) {
            		log.debug("yes");
            		return true;
            	}
            	
            	return false;
            	
//                final String resourceName = clazzName.replace('.', '/') + ".class";
//                final URL resource = pClassLoader.getResource(resourceName);
//                return resource == null;
            }

            public boolean isPackage( char[][] parentPackageName, char[] packageName ) {
                final StringBuffer result = new StringBuffer();
                if (parentPackageName != null) {
                    for (int i = 0; i < parentPackageName.length; i++) {
                        if (i != 0) {
                            result.append('.');
                        }
                        result.append(parentPackageName[i]);
                    }
                }
                
//                log.debug("isPackage parentPackageName=" + result.toString() + " packageName=" + new String(packageName));
                
//                if (Character.isUpperCase(packageName[0])) {
//                    return false;
//                }
                
                if (parentPackageName != null && parentPackageName.length > 0) {
                    result.append('.');
                }
                result.append(packageName);
                return isPackage(result.toString());
            }

            public void cleanup() {
            	log.debug("cleanup");
            }
        };

        final Collection problems = new ArrayList();
        final ICompilerRequestor compilerRequestor = new ICompilerRequestor() {
            public void acceptResult( final CompilationResult result ) {
                if (result.hasProblems()) {
                    final IProblem[] iproblems = result.getProblems();
                    for (int i = 0; i < iproblems.length; i++) {
                        final IProblem iproblem = iproblems[i];
                        final CompilationProblem problem = new EclipseCompilationProblem(iproblem); 
                        if (problemHandler != null) {
                            problemHandler.handle(problem);
                        }
                        problems.add(problem);
                    }
                }
                if (!result.hasErrors()) {
                    final ClassFile[] clazzFiles = result.getClassFiles();
                    for (int i = 0; i < clazzFiles.length; i++) {
                        final ClassFile clazzFile = clazzFiles[i];
                        final char[][] compoundName = clazzFile.getCompoundName();
                        final StringBuffer clazzName = new StringBuffer();
                        for (int j = 0; j < compoundName.length; j++) {
                            if (j != 0) {
                                clazzName.append('.');
                            }
                            clazzName.append(compoundName[j]);
                        }
                        pStore.write(clazzName.toString().replace('.', '/') + ".class", clazzFile.getBytes());
                    }
                }
            }
        };

        final Compiler compiler = new Compiler(nameEnvironment, policy, settingsMap, compilerRequestor, problemFactory, false);

        compiler.compile(compilationUnits);

        final CompilationProblem[] result = new CompilationProblem[problems.size()];
        problems.toArray(result);
        return new org.apache.commons.jci.compilers.CompilationResult(result);
    }
}
