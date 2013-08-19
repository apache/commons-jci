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

import org.apache.commons.jci.problems.CompilationProblem;

/**
 * 
 * @author tcurdt
 */
public class JavacCompilationProblem implements CompilationProblem {

    private final int endCoumn;
    private final int endLine;
    private final String fileName;
    private final String message;
    private final int startCoumn;
    private final int startLine;
    private final boolean isError;

    public JavacCompilationProblem(String message, boolean isError) {
        this.message = message;
        this.isError = isError;
        this.fileName = "";
        this.startLine = 0;
        this.startCoumn = 0;
        this.endCoumn = 0;
        this.endLine = 0;
    }

    public JavacCompilationProblem(String fileName, boolean isError, int startLine, int startCoumn, int endLine, int endCoumn, String message) {
        this.message = message;
        this.isError = isError;
        this.fileName = fileName;
        this.startCoumn = startCoumn;
        this.endCoumn = endCoumn;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public int getEndColumn() {
        return endCoumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMessage() {
        return message;
    }

    public int getStartColumn() {
        return startCoumn;
    }

    public int getStartLine() {
        return startLine;
    }

    public boolean isError() {
        return isError;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getFileName()).append(" (");
        sb.append(getStartLine());
        sb.append(":");
        sb.append(getStartColumn());
        sb.append(") : ");
        sb.append(getMessage());
        return sb.toString();
    }
}
