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

package org.apache.commons.jci2.examples.commandline;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.jci2.core.compiler.CompilationResult;
import org.apache.commons.jci2.core.compiler.JavaCompiler;
import org.apache.commons.jci2.core.compiler.JavaCompilerFactory;
import org.apache.commons.jci2.core.compiler.JavaCompilerSettings;
import org.apache.commons.jci2.core.problems.CompilationProblem;
import org.apache.commons.jci2.core.problems.CompilationProblemHandler;
import org.apache.commons.jci2.core.readers.FileResourceReader;
import org.apache.commons.jci2.core.readers.ResourceReader;
import org.apache.commons.jci2.core.stores.FileResourceStore;
import org.apache.commons.jci2.core.stores.ResourceStore;

/**
 * A simple front end to jci2 mimicking the javac command line
 *
 * @author tcurdt
 */
public final class CommandlineCompiler {

    public static void main( final String[] args ) throws Exception {

        final Options options = new Options();

        options.addOption(
                OptionBuilder.withArgName("a.jar:b.jar")
                    .hasArg()
                    .withValueSeparator( ':' )
                    .withDescription("Specify where to find user class files")
                    .create( "classpath" ));

        options.addOption(
                OptionBuilder.withArgName("release")
                    .hasArg()
                    .withDescription("Provide source compatibility with specified release")
                    .create( "source" ));

        options.addOption(
                OptionBuilder.withArgName("release")
                    .hasArg()
                    .withDescription("Generate class files for specific VM version")
                    .create( "target" ));

        options.addOption(
                OptionBuilder.withArgName("path")
                    .hasArg()
                    .withDescription("Specify where to find input source files")
                    .create( "sourcepath" ));

        options.addOption(
                OptionBuilder.withArgName("directory")
                    .hasArg()
                    .withDescription("Specify where to place generated class files")
                    .create( "d" ));

        options.addOption(
                OptionBuilder.withArgName("num")
                    .hasArg()
                    .withDescription("Stop compilation after these number of errors")
                    .create( "Xmaxerrs" ));

        options.addOption(
                OptionBuilder.withArgName("num")
                    .hasArg()
                    .withDescription("Stop compilation after these number of warning")
                    .create( "Xmaxwarns" ));

        options.addOption(
                OptionBuilder.withDescription("Generate no warnings")
                    .create( "nowarn" ));

//        final HelpFormatter formatter = new HelpFormatter();
//        formatter.printHelp("jci2", options);

        final CommandLineParser parser = new GnuParser();
        final CommandLine cmd = parser.parse(options, args, true);

        ClassLoader classloader = CommandlineCompiler.class.getClassLoader();
        File sourcepath = new File(".");
        File targetpath = new File(".");
        int maxerrs = 10;
        int maxwarns = 10;
        final boolean nowarn = cmd.hasOption("nowarn");

        final JavaCompiler compiler = new JavaCompilerFactory().createCompiler("eclipse");
        final JavaCompilerSettings settings = compiler.createDefaultSettings();

        for (final Iterator it = cmd.iterator(); it.hasNext();) {
            final Option option = (Option) it.next();
            if ("classpath".equals(option.getOpt())) {
                final String[] values = option.getValues();
                final URL[] urls = new URL[values.length];
                for (int i = 0; i < urls.length; i++) {
                    urls[i] = new File(values[i]).toURL();
                }
                classloader = new URLClassLoader(urls);
            } else if ("source".equals(option.getOpt())) {
                settings.setSourceVersion(option.getValue());
            } else if ("target".equals(option.getOpt())) {
                settings.setTargetVersion(option.getValue());
            } else if ("sourcepath".equals(option.getOpt())) {
                sourcepath = new File(option.getValue());
            } else if ("d".equals(option.getOpt())) {
                targetpath = new File(option.getValue());
            } else if ("Xmaxerrs".equals(option.getOpt())) {
                maxerrs = Integer.parseInt(option.getValue());
            } else if ("Xmaxwarns".equals(option.getOpt())) {
                maxwarns = Integer.parseInt(option.getValue());
            }
        }

        final ResourceReader reader = new FileResourceReader(sourcepath);
        final ResourceStore store = new FileResourceStore(targetpath);

        final int maxErrors = maxerrs;
        final int maxWarnings = maxwarns;
        compiler.setCompilationProblemHandler(new CompilationProblemHandler() {
            int errors = 0;
            int warnings = 0;
            public boolean handle(final CompilationProblem pProblem) {

                if (pProblem.isError()) {
                    System.err.println(pProblem);

                    errors++;

                    if (errors >= maxErrors) {
                        return false;
                    }
                } else {
                    if (!nowarn) {
                        System.err.println(pProblem);
                    }

                    warnings++;

                    if (warnings >= maxWarnings) {
                        return false;
                    }
                }

                return true;
            }
        });

        final String[] resource = cmd.getArgs();

        for (final String element : resource) {
            System.out.println("compiling " + element);
        }

        final CompilationResult result = compiler.compile(resource, reader, store, classloader);

        System.out.println( result.getErrors().length + " errors");
        System.out.println( result.getWarnings().length + " warnings");

    }
}
