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

package org.apache.commons.jci2.compiler.groovy;

import org.apache.commons.jci2.core.problems.CompilationProblem;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

/**
 * Groovy version of a CompilationProblem
 */
public final class GroovyCompilationProblem implements CompilationProblem {

    private final String fileName;
    private final String message;
    private final boolean error;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;

    public GroovyCompilationProblem(final Message pMessage) {
        if (pMessage instanceof SimpleMessage) {
            error = false;
        } else {
            error = true;
        }
        if (pMessage instanceof SyntaxErrorMessage) {
            final SyntaxErrorMessage syntaxErrorMessage = (SyntaxErrorMessage)pMessage;
            final SyntaxException syntaxException = syntaxErrorMessage.getCause();
            message = syntaxException.getMessage();
            fileName = syntaxException.getSourceLocator();
            // FIXME: getStartLine() vs. getLine()
            startLine = syntaxException.getStartLine();
            startColumn = syntaxException.getStartColumn();
            endLine = syntaxException.getLine();
            endColumn = syntaxException.getEndColumn();
        } else {
            fileName = "";
            startLine = 0;
            startColumn = 0;
            endLine = 0;
            endColumn = 0;
            if (pMessage instanceof ExceptionMessage) {
                message = ((ExceptionMessage)pMessage).getCause().getMessage();
            } else if (pMessage instanceof SimpleMessage) {
                message = ((SimpleMessage)pMessage).getMessage();
            } else {
                message = pMessage.toString();
            }
        }
    }

    @Override
    public boolean isError() {
        return error;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public int getStartLine() {
        return startLine;
    }

    @Override
    public int getStartColumn() {
        return startColumn;
    }

    @Override
    public int getEndLine() {
        return endLine;
    }

    @Override
    public int getEndColumn() {
        return endColumn;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return getMessage();
    }

}
