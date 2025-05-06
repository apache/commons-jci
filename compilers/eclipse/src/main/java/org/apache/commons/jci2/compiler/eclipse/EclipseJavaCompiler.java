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
package org.apache.commons.jci2.compiler.eclipse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.jci2.core.compiler.AbstractJavaCompiler;
import org.apache.commons.jci2.core.compiler.JavaCompilerSettings;
import org.apache.commons.jci2.core.problems.CompilationProblem;
import org.apache.commons.jci2.core.readers.ResourceReader;
import org.apache.commons.jci2.core.stores.ResourceStore;
import org.apache.commons.jci2.core.utils.ConversionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

/**
 * Eclipse compiler implemenation
 */
public final class EclipseJavaCompiler extends AbstractJavaCompiler {

    private final Log log = LogFactory.getLog(EclipseJavaCompiler.class);
    private final EclipseJavaCompilerSettings defaultSettings;

    public EclipseJavaCompiler() {
        this(new EclipseJavaCompilerSettings());
    }

    public EclipseJavaCompiler( final Map<String, String> settings ) {
        defaultSettings = new EclipseJavaCompilerSettings(settings);
    }

    public EclipseJavaCompiler( final EclipseJavaCompilerSettings settings ) {
        defaultSettings = settings;
    }

    final class CompilationUnit implements ICompilationUnit {

        final private String clazzName;
        final private String fileName;
        final private char[] typeName;
        final private char[][] packageName;
        final private ResourceReader reader;

        CompilationUnit( final ResourceReader reader, final String sourceFile ) {
            this.reader = reader;
            clazzName = ConversionUtils.convertResourceToClassName(sourceFile);
            fileName = sourceFile;
            final int dot = clazzName.lastIndexOf('.');
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

        @Override
        public char[] getFileName() {
            return fileName.toCharArray();
        }

        @Override
        public char[] getContents() {
            final byte[] content = reader.getBytes(fileName);

            if (content == null) {
                return null;
                //throw new RuntimeException("resource " + fileName + " could not be found");
            }

            return new String(content).toCharArray();
        }

        @Override
        public char[] getMainTypeName() {
            return typeName;
        }

        @Override
        public char[][] getPackageName() {
            return packageName;
        }

        @Override
        public boolean ignoreOptionalProblems() {
            return false;
        }
    }

    @Override
    public org.apache.commons.jci2.core.compiler.CompilationResult compile(
            final String[] sourceFiles,
            final ResourceReader resourceReader,
            final ResourceStore resourceStore,
            final ClassLoader classLoader,
            final JavaCompilerSettings settings
            ) {

        final Map<String, String> settingsMap = new EclipseJavaCompilerSettings(settings).toNativeSettings();

        final Collection<CompilationProblem> problems = new ArrayList<>();

        final ICompilationUnit[] compilationUnits = new ICompilationUnit[sourceFiles.length];
        for (int i = 0; i < compilationUnits.length; i++) {
            final String sourceFile = sourceFiles[i];

            if (resourceReader.isAvailable(sourceFile)) {
                compilationUnits[i] = new CompilationUnit(resourceReader, sourceFile);
                log.debug("compiling " + sourceFile);
            } else {
                // log.error("source not found " + sourceFile);

                final CompilationProblem problem = new CompilationProblem() {

                    @Override
                    public int getEndColumn() {
                        return 0;
                    }

                    @Override
                    public int getEndLine() {
                        return 0;
                    }

                    @Override
                    public String getFileName() {
                        return sourceFile;
                    }

                    @Override
                    public String getMessage() {
                        return "Source " + sourceFile + " could not be found";
                    }

                    @Override
                    public int getStartColumn() {
                        return 0;
                    }

                    @Override
                    public int getStartLine() {
                        return 0;
                    }

                    @Override
                    public boolean isError() {
                        return true;
                    }

                    @Override
                    public String toString() {
                        return getMessage();
                    }
                };

                if (problemHandler != null) {
                    problemHandler.handle(problem);
                }

                problems.add(problem);
            }
        }

        if (!problems.isEmpty()) {
            final CompilationProblem[] result = new CompilationProblem[problems.size()];
            problems.toArray(result);
            return new org.apache.commons.jci2.core.compiler.CompilationResult(result);
        }

        final IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.proceedWithAllProblems();
        final IProblemFactory problemFactory = new DefaultProblemFactory(Locale.getDefault());
        final INameEnvironment nameEnvironment = new INameEnvironment() {

            @Override
            public NameEnvironmentAnswer findType( final char[][] compoundTypeName ) {
                final StringBuilder result = new StringBuilder();
                for (int i = 0; i < compoundTypeName.length; i++) {
                    if (i != 0) {
                        result.append('.');
                    }
                    result.append(compoundTypeName[i]);
                }

                //log.debug("finding compoundTypeName=" + result.toString());

                return findType(result.toString());
            }

            @Override
            public NameEnvironmentAnswer findType( final char[] typeName, final char[][] packageName ) {
                final StringBuilder result = new StringBuilder();
                for (final char[] element : packageName) {
                    result.append(element);
                    result.append('.');
                }

//                log.debug("finding typeName=" + new String(typeName) + " packageName=" + result.toString());

                result.append(typeName);
                return findType(result.toString());
            }

            private NameEnvironmentAnswer findType( final String className ) {

                if (isPackage(className)) {
                    return null;
                }

                log.debug("finding " + className);

                final String resourceName = ConversionUtils.convertClassToResourcePath(className);

                final byte[] clazzBytes = resourceStore.read(resourceName);
                if (clazzBytes != null) {
                    log.debug("loading from store " + className);

                    final char[] fileName = className.toCharArray();
                    try {
                        final ClassFileReader classFileReader = new ClassFileReader(clazzBytes, fileName, true);
                        return new NameEnvironmentAnswer(classFileReader, null);
                    } catch (final ClassFormatException e) {
                        log.error("wrong class format", e);
                        return null;
                    }
                }

                log.debug("not in store " + className);

                final InputStream is = classLoader.getResourceAsStream(resourceName);
                if (is == null) {
                    log.debug("class " + className + " not found");
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
                    final char[] fileName = className.toCharArray();
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

            private boolean isPackage( final String className ) {

                // reject this early as it is cheap
                if (className.contains("-")) { // "-" is not valid in package names
                    return false;
                }

                final InputStream is = classLoader.getResourceAsStream(ConversionUtils.convertClassToResourcePath(className));
                if (is != null) {
                    log.debug("found the class for " + className + "- no package");
                    try {
                        is.close();
                    } catch (final IOException ie) {
                        log.error("could not close input stream", ie);
                    }
                    return false;
                }

                // FIXME: this should not be tied to the extension
                final String source = className.replace('.', '/') + ".java";
                if (resourceReader.isAvailable(source)) {
                    log.debug("found the source " + source + " for " + className + " - no package ");
                    return false;
                }

                /*
                 * See https://issues.apache.org/jira/browse/JCI-59
                 * At present, the code assumes that anything else is a package name
                 * This is wrong, as for example jci2.AdditionalTopLevel is not a package name.
                 * It's not clear how to fix this in general.
                 * It would seem to need access to the input classpath and/or the generated classes.
                 */
                return true;
            }

            @Override
            public boolean isPackage( final char[][] parentPackageName, final char[] packageName ) {
                final StringBuilder result = new StringBuilder();
                if (parentPackageName != null) {
                    for (int i = 0; i < parentPackageName.length; i++) {
                        if (i != 0) {
                            result.append('.');
                        }
                        result.append(parentPackageName[i]);
                    }
                }

//                log.debug("isPackage parentPackageName=" + result.toString() + " packageName=" + new String(packageName));

                if (parentPackageName != null && parentPackageName.length > 0) {
                    result.append('.');
                }
                result.append(packageName);
                return isPackage(result.toString());
            }

            @Override
            public void cleanup() {
                log.debug("cleanup");
            }
        };

        final ICompilerRequestor compilerRequestor = result -> {
            if (result.hasProblems()) {
                for (final IProblem iproblem : result.getProblems()) {
                    final CompilationProblem problem = new EclipseCompilationProblem(iproblem);
                    if (problemHandler != null) {
                        problemHandler.handle(problem);
                    }
                    problems.add(problem);
                }
            }
            if (!result.hasErrors()) {
                final ClassFile[] clazzFiles = result.getClassFiles();
                for (final ClassFile clazzFile : clazzFiles) {
                    final char[][] compoundName = clazzFile.getCompoundName();
                    final StringBuilder clazzName = new StringBuilder();
                    for (int j = 0; j < compoundName.length; j++) {
                        if (j != 0) {
                            clazzName.append('.');
                        }
                        clazzName.append(compoundName[j]);
                    }
                    resourceStore.write(clazzName.toString().replace('.', '/') + ".class", clazzFile.getBytes());
                }
            }
        };

        final Compiler compiler = new Compiler(nameEnvironment, policy, new CompilerOptions(settingsMap), compilerRequestor, problemFactory);

        compiler.compile(compilationUnits);

        final CompilationProblem[] result = new CompilationProblem[problems.size()];
        problems.toArray(result);
        return new org.apache.commons.jci2.core.compiler.CompilationResult(result);
    }

    @Override
    public JavaCompilerSettings createDefaultSettings() {
        return new EclipseJavaCompilerSettings(defaultSettings);
    }
}
