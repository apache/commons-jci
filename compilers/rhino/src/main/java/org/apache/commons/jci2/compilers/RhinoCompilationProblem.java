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

package org.apache.commons.jci2.compilers;

import org.apache.commons.jci2.problems.CompilationProblem;

/**
 *
 * @author tcurdt
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

    public int getEndColumn() {
        return column;
    }

    public int getEndLine() {
        return line;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMessage() {
        return message;
    }

    public int getStartColumn() {
        return column;
    }

    public int getStartLine() {
        return line;
    }

    public boolean isError() {
        return error;
    }

}
