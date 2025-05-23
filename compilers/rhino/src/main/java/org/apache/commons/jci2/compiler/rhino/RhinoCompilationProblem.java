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

package org.apache.commons.jci2.compiler.rhino;

import org.apache.commons.jci2.core.problems.CompilationProblem;

/**
 */
public final class RhinoCompilationProblem implements CompilationProblem {

    private final String message;
    private final String fileName;
    private final int line;
    private final int column;
    private final boolean error;

    public RhinoCompilationProblem( final String pMessage, final String pFileName, final int pLine, final String pScript, final int pColumn, final boolean pError ) {
        message = pMessage;
        fileName = pFileName;
        line = pLine;
        column = pColumn;
        error = pError;
    }

    @Override
    public int getEndColumn() {
        return column;
    }

    @Override
    public int getEndLine() {
        return line;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStartColumn() {
        return column;
    }

    @Override
    public int getStartLine() {
        return line;
    }

    @Override
    public boolean isError() {
        return error;
    }

}
