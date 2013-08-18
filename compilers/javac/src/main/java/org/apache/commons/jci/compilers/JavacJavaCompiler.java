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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;

/**
 * Compiler leveraging the javac from tools.jar. Using byte code rewriting
 * it is tricked not to read/write from/to disk but use the ResourceReader and
 * ResourceStore provided instead.
 * 
 * NOTE: (As of now) this compiler only works up until java5. Java6 comes with
 * a new API based on jsr199. So please use that jsr199 compiler instead.
 *   
 * @author tcurdt
 * @todo classpath and settings support
 */
public final class JavacJavaCompiler extends AbstractJavaCompiler {

    private static final String EOL = System.getProperty("line.separator");
    private static final String WARNING_PREFIX = "warning: ";
    private static final String NOTE_PREFIX = "Note: ";
    private static final String ERROR_PREFIX = "error: ";

    private final JavacJavaCompilerSettings defaultSettings;

    public JavacJavaCompiler() {
        defaultSettings = new JavacJavaCompilerSettings();
    }

    public JavacJavaCompiler( final JavacJavaCompilerSettings pSettings ) {
        defaultSettings = pSettings;
    }

    public CompilationResult compile( final String[] pSourcePaths, final ResourceReader pReader, ResourceStore pStore, final ClassLoader pClasspathClassLoader, final JavaCompilerSettings pSettings ) {

        try {
            final ClassLoader cl = new JavacClassLoader(pClasspathClassLoader);
            final Class renamedClass = cl.loadClass("com.sun.tools.javac.Main");

            FileInputStreamProxy.setResourceReader(pReader);
            FileOutputStreamProxy.setResourceStore(pStore);

            final Method compile = renamedClass.getMethod("compile", new Class[] { String[].class, PrintWriter.class });
            final StringWriter out = new StringWriter();

            final String[] compilerArguments = buildCompilerArguments(new JavacJavaCompilerSettings(pSettings), pSourcePaths, pClasspathClassLoader);
                        
            final Integer ok = (Integer) compile.invoke(null, new Object[] { compilerArguments, new PrintWriter(out) });

            final CompilationResult result = parseModernStream(new BufferedReader(new StringReader(out.toString())));

            if (result.getErrors().length == 0 && ok.intValue() != 0) {
                return new CompilationResult(new CompilationProblem[] {
                        new JavacCompilationProblem("Failure executing javac, but could not parse the error: " + out.toString(), true) });
            }

            return result;

        } catch(Exception e) {
            return new CompilationResult(new CompilationProblem[] {
                    new JavacCompilationProblem("Error while executing the compiler: " + e.toString(), true) });
        } finally {
            // help GC
            FileInputStreamProxy.setResourceReader(null);
            FileOutputStreamProxy.setResourceStore(null);
        }
    }

    private CompilationResult parseModernStream( final BufferedReader pReader ) throws IOException {
        final List problems = new ArrayList();
        String line;

        while (true) {
            // cleanup the buffer
            final StringBuilder buffer = new StringBuilder();

            // most errors terminate with the '^' char
            do {
                line = pReader.readLine();
                if (line == null) {
                    return new CompilationResult((CompilationProblem[]) problems.toArray(new CompilationProblem[problems.size()]));
                }

                // TODO: there should be a better way to parse these
                if (buffer.length() == 0 && line.startsWith(ERROR_PREFIX)) {
                    problems.add(new JavacCompilationProblem(line, true));
                }
                else if (buffer.length() == 0 && line.startsWith(NOTE_PREFIX)) {
                    // skip this one - it is JDK 1.5 telling us that the
                    // interface is deprecated.
                } else {
                    buffer.append(line);
                    buffer.append(EOL);
                }
            } while (!line.endsWith("^"));

            // add the error
            problems.add(parseModernError(buffer.toString()));
        }
    }

    private CompilationProblem parseModernError( final String pError ) {
        final StringTokenizer tokens = new StringTokenizer(pError, ":");
        boolean isError = true;
        try {
            String file = tokens.nextToken();
            // When will this happen?
            if (file.length() == 1) {
                file = new StringBuilder(file).append(":").append(
                        tokens.nextToken()).toString();
            }
            final int line = Integer.parseInt(tokens.nextToken());
            final StringBuilder msgBuffer = new StringBuilder();

            String msg = tokens.nextToken(EOL).substring(2);
            isError = !msg.startsWith(WARNING_PREFIX);

            // Remove the 'warning: ' prefix
            if (!isError) {
                msg = msg.substring(WARNING_PREFIX.length());
            }
            msgBuffer.append(msg);

            String context = tokens.nextToken(EOL);
            String pointer = tokens.nextToken(EOL);

            if (tokens.hasMoreTokens()) {
                msgBuffer.append(EOL);
                msgBuffer.append(context); // 'symbol' line
                msgBuffer.append(EOL);
                msgBuffer.append(pointer); // 'location' line
                msgBuffer.append(EOL);

                context = tokens.nextToken(EOL);

                try {
                    pointer = tokens.nextToken(EOL);
                } catch (NoSuchElementException e) {
                    pointer = context;
                    context = null;
                }
            }
            final String message = msgBuffer.toString();
            int startcolumn = pointer.indexOf("^");
            int endcolumn = context == null ? startcolumn : context.indexOf(" ", startcolumn);
            if (endcolumn == -1) {
                endcolumn = context.length();
            }
            return new JavacCompilationProblem(file, isError, line, startcolumn, line, endcolumn, message);
        }
        catch (NoSuchElementException e) {
            return new JavacCompilationProblem("no more tokens - could not parse error message: " + pError, isError);
        }
        catch (NumberFormatException e) {
            return new JavacCompilationProblem("could not parse error message: " + pError, isError);
        }
        catch (Exception e) {
            return new JavacCompilationProblem("could not parse error message: " + pError, isError);
        }
    }

    public JavaCompilerSettings createDefaultSettings() {
        return new JavacJavaCompilerSettings(defaultSettings);
    }

    private String[] buildCompilerArguments( final JavacJavaCompilerSettings pSettings, final String[] pResourcePaths, final ClassLoader pClassloader ) {

        // FIXME: build classpath from classloader information
    	final String[] classpath = new String[0];
    	final String[] resources = pResourcePaths;    	
    	final String[] args = pSettings.toNativeSettings();
    	
    	final String[] result = new String[classpath.length + resources.length + args.length];
    	
    	System.arraycopy(classpath, 0, result, 0, classpath.length);
    	System.arraycopy(resources, 0, result, classpath.length, resources.length);
    	System.arraycopy(args, 0, result, classpath.length + resources.length, args.length);
    	
    	return result;
    }
    
}
