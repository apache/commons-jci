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

import groovy.lang.GroovyClassLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.utils.ConversionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.tools.GroovyClass;

/**
 * Groovy implementation of the JavaCompiler interface
 * 
 * @author tcurdt
 */
public final class GroovyJavaCompiler extends AbstractJavaCompiler {

    private final Log log = LogFactory.getLog(GroovyJavaCompiler.class);
    private final GroovyJavaCompilerSettings defaultSettings;
    
    public GroovyJavaCompiler() {
        defaultSettings = new GroovyJavaCompilerSettings(new CompilerConfiguration());
    }
        
    public CompilationResult compile(
            final String[] pResourceNames,
            final ResourceReader pReader,
            final ResourceStore pStore,
            final ClassLoader pClassLoader,
            final JavaCompilerSettings pSettings
            ) {

        final CompilerConfiguration configuration = ((GroovyJavaCompilerSettings) pSettings).getCompilerConfiguration();
        final ErrorCollector collector = new ErrorCollector(configuration);
        final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(pClassLoader);
        final CompilationUnit unit = new CompilationUnit(configuration, null, groovyClassLoader);
        final SourceUnit[] source = new SourceUnit[pResourceNames.length];
        for (int i = 0; i < source.length; i++) {
            final String resourceName = pResourceNames[i];
            source[i] = new SourceUnit(
                    ConversionUtils.convertResourceToClassName(resourceName),
                    new String(pReader.getBytes(resourceName)), // FIXME delay the read
                    configuration,
                    groovyClassLoader,
                    collector
                    );
            unit.addSource(source[i]);
        }
        
        final Collection<CompilationProblem> problems = new ArrayList<CompilationProblem>();

        try {
            log.debug("compiling");
            unit.compile(Phases.CLASS_GENERATION);
            
            @SuppressWarnings("unchecked") // Groovy library is not yet generic
            final List<GroovyClass> classes = unit.getClasses();
            for (GroovyClass clazz : classes) {
                final byte[] bytes = clazz.getBytes();
                pStore.write(ConversionUtils.convertClassToResourcePath(clazz.getName()), bytes);
            }
        } catch (final MultipleCompilationErrorsException e) {
            final ErrorCollector col = e.getErrorCollector();
            @SuppressWarnings("unchecked") // Groovy library is not yet generic
            final Collection<WarningMessage> warnings = col.getWarnings();
            if (warnings != null) {
                for (WarningMessage warning : warnings) {
                    final CompilationProblem problem = new GroovyCompilationProblem(warning); 
                    if (problemHandler != null) {
                        problemHandler.handle(problem);
                    }
                    problems.add(problem);
                }
            }

            @SuppressWarnings("unchecked") // Groovy library is not yet generic
            final Collection<Message> errors = col.getErrors();
            if (errors != null) {
                for (Message message : errors) {
                    final CompilationProblem problem = new GroovyCompilationProblem(message); 
                    if (problemHandler != null) {
                        problemHandler.handle(problem);
                    }
                    problems.add(problem);
                }
            }
        } catch (CompilationFailedException e) {
            throw new RuntimeException("no expected");
        }

        final CompilationProblem[] result = new CompilationProblem[problems.size()];
        problems.toArray(result);
        return new CompilationResult(result);
    }

    public JavaCompilerSettings createDefaultSettings() {
        return defaultSettings;
    }
}
