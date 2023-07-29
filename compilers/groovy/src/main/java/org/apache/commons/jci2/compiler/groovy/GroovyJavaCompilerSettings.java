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

package org.apache.commons.jci2.compiler.groovy;

import org.apache.commons.jci2.core.compiler.JavaCompilerSettings;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * Native configuration for the Groovy compiler
 *
 * @author tcurdt
 */
public final class GroovyJavaCompilerSettings extends JavaCompilerSettings {

    private final CompilerConfiguration settings;

    public GroovyJavaCompilerSettings( final CompilerConfiguration pSettings ) {
        settings = pSettings;
    }

    public CompilerConfiguration getCompilerConfiguration() {
        return settings;
    }

}
