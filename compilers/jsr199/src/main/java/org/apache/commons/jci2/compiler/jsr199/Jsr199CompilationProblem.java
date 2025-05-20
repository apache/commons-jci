/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jci2.compiler.jsr199;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.apache.commons.jci2.core.problems.CompilationProblem;

public class Jsr199CompilationProblem implements CompilationProblem {

    final Diagnostic<? extends JavaFileObject> problem;

    public Jsr199CompilationProblem( final Diagnostic<? extends JavaFileObject> pProblem ) {
        problem = pProblem;
    }

    @Override
    public int getEndColumn() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getEndLine() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getStartColumn() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getStartLine() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isError() {
        // TODO Auto-generated method stub
        return false;
    }

}
