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
package org.apache.commons.jci.compilers.eclipse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.problems.CompilationProblemHandler;
import org.apache.commons.jci.problems.ConsoleCompilationProblemHandler;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;
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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

public final class EclipseJavaCompiler implements JavaCompiler {

    private final static Log log = LogFactory.getLog(EclipseJavaCompiler.class);

    final class CompilationUnit implements ICompilationUnit {

        final private String clazzName;
        final private String fileName;
        final private char[] typeName;
        final private char[][] packageName;
        final private ResourceReader reader;

        CompilationUnit(final ResourceReader pReader, final String pClazzName) {
            reader = pReader;

            clazzName = pClazzName;

            fileName = StringUtils.replaceChars(clazzName, '.', '/') + ".java";

            int dot = clazzName.lastIndexOf('.');
            if (dot > 0) {
                typeName = clazzName.substring(dot + 1).toCharArray();
            } else {
                typeName = clazzName.toCharArray();
            }

            final StringTokenizer izer = new StringTokenizer(clazzName, ".");
            packageName = new char[izer.countTokens() - 1][];
            for (int i = 0; i < packageName.length; i++) {
                packageName[i] = izer.nextToken().toCharArray();
            }
        }

        public char[] getFileName() {
            return fileName.toCharArray();
        }

        public char[] getContents() {
            return reader.getContent(fileName);
        }

        public char[] getMainTypeName() {
            return typeName;
        }

        public char[][] getPackageName() {
            return packageName;
        }

    }

    public void compile(
            final String[] clazzNames,
            final ResourceReader reader,
            final ResourceStore store,
            final CompilationProblemHandler problemHandler
            ) {

        final Map settings = new HashMap();
        settings.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.GENERATE);
        settings.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.GENERATE);
        settings.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
        settings.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.IGNORE);
        settings.put(CompilerOptions.OPTION_Encoding, "UTF-8");
        settings.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
        settings.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
        settings.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);

        final Set clazzIndex = new HashSet();
        ICompilationUnit[] compilationUnits = new ICompilationUnit[clazzNames.length];
        for (int i = 0; i < compilationUnits.length; i++) {
            final String clazzName = clazzNames[i];
            compilationUnits[i] = new CompilationUnit(reader, clazzName);
            clazzIndex.add(clazzName);
            log.debug("compiling " + clazzName);
        }
        
        final IErrorHandlingPolicy policy = DefaultErrorHandlingPolicies.proceedWithAllProblems();
        final IProblemFactory problemFactory = new DefaultProblemFactory(Locale.getDefault());
        final INameEnvironment nameEnvironment = new INameEnvironment() {

            public NameEnvironmentAnswer findType( final char[][] compoundTypeName ) {
                //log.debug("NameEnvironment.findType compound");

                final StringBuffer result = new StringBuffer();
                for (int i = 0; i < compoundTypeName.length; i++) {
                    if (i != 0) {
                        result.append('.');
                    }
                    result.append(compoundTypeName[i]);
                }

                return findType(result.toString());
            }

            public NameEnvironmentAnswer findType( final char[] typeName, final char[][] packageName ) {
                //log.debug("NameEnvironment.findType");

                final StringBuffer result = new StringBuffer();
                for (int i = 0; i < packageName.length; i++) {
                    if (i != 0) {
                        result.append('.');
                    }
                    result.append(packageName[i]);
                }

                result.append('.');
                result.append(typeName);

                return findType(result.toString());

            }

            private NameEnvironmentAnswer findType(final String clazzName) {
                //log.debug("NameEnvironment.findType " + clazzName);

                byte[] clazzBytes = store.read(clazzName);
                if (clazzBytes != null) {
                    //log.debug("loading from store " + clazzName);

                    final char[] fileName = clazzName.toCharArray();
                    try {
                        ClassFileReader classFileReader = new ClassFileReader(clazzBytes, fileName, true);
                        return new NameEnvironmentAnswer(classFileReader, null);
                    } catch (ClassFormatException e) {
                        e.printStackTrace();
                    }                    

                }
                else {

                    if (reader.isAvailable(clazzName.replace('.', '/') + ".java")) {
	                    log.debug("compile " + clazzName);
	                    ICompilationUnit compilationUnit = new CompilationUnit(reader, clazzName);
	                    return new NameEnvironmentAnswer(compilationUnit, null);                                            
                    }
                    else {
                        final String resourceName = clazzName.replace('.', '/') + ".class";
                        final InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourceName);
                        
                        if (is != null) {
                            
    	                    //log.debug("loading from classloader " + clazzName);
                            final byte[] buffer = new byte[8192];
                            ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
                            int count;
                            try {
                                while ((count = is.read(buffer, 0, buffer.length)) > 0) {
                                    baos.write(buffer, 0, count);
                                }
                                baos.flush();
                                clazzBytes = baos.toByteArray();
                                final char[] fileName = clazzName.toCharArray();
                                ClassFileReader classFileReader = new ClassFileReader(clazzBytes, fileName, true);
                                return new NameEnvironmentAnswer(classFileReader, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

    	                }
                    }
                }
                //log.debug("not found " + clazzName);
                return null;    	                    
            }

            private boolean isPackage( final String clazzName ) {

                final String resourceName = clazzName.replace('.', '/') + ".class";
                final InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourceName);
                boolean result = (is == null);
                //log.debug("NameEnvironment.isPackage " + resourceName + " = " + result);
                return result;
            }

            public boolean isPackage(char[][] parentPackageName, char[] packageName) {

                final StringBuffer result = new StringBuffer();

                if (parentPackageName != null) {

                    for (int i = 0; i < parentPackageName.length; i++) {
                        if (i != 0) {
                            result.append('.');
                        }
                        result.append(parentPackageName[i]);
                    }
                }

                if (Character.isUpperCase(packageName[0])) {
                    return false;
                    /*
                    if (!isPackage(result.toString())) {
                        return false;
                    }*/
                }

                if (parentPackageName != null && parentPackageName.length > 0) {
                    result.append('.');
                }
                result.append(packageName);

                return isPackage(result.toString());
            }

            public void cleanup() {
            }

        };
        final ICompilerRequestor compilerRequestor = new ICompilerRequestor() {

            public void acceptResult(CompilationResult result) {
                try {
                    if (result.hasProblems()) {
                        if (problemHandler != null) {
	                        final IProblem[] problems = result.getProblems();
	                        for (int i = 0; i < problems.length; i++) {
	                            final IProblem problem = problems[i];
	                            problemHandler.handle(
	                                    new CompilationProblem(
	                                            problem.getID(),
	                                            new String(problem.getOriginatingFileName()),
	                                            problem.getMessage(),
	                                            problem.getSourceLineNumber(),
	                                            problem.getSourceLineNumber(),
	                                            problem.isError()
	                                            ));
	                        }
                        }
                    } else {

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

                            store.write(clazzName.toString(), clazzFile.getBytes());
                        }
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }

        };

        final Compiler compiler = new Compiler(nameEnvironment, policy, settings, compilerRequestor, problemFactory);

        compiler.compile(compilationUnits);

    }

    public static void main(String[] args) throws Exception {

        final JavaCompiler compiler = new EclipseJavaCompiler();
        final ConsoleCompilationProblemHandler problemHandler = new ConsoleCompilationProblemHandler();
        
        compiler.compile(
                args,
                new FileResourceReader("classes"),
                new MemoryResourceStore(),
                problemHandler
                );
        
        log.debug(
                problemHandler.getErrorCount() + " errors, " +
                problemHandler.getWarningCount() + " warnings"
                );
    }
}
