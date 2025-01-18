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

package org.apache.commons.jci2.compiler.janino;

import org.apache.commons.jci2.core.problems.CompilationProblem;
import org.codehaus.commons.compiler.LocatedException;
import org.codehaus.commons.compiler.Location;

/**
 * Janino version of a CompilationProblem
 *
 * @author tcurdt
 */
public final class JaninoCompilationProblem implements CompilationProblem {

    private final Location location;
    private final String fileName;
    private final String message;
    private final boolean error;

    public JaninoCompilationProblem(final LocatedException locatedException) {
        this(locatedException.getLocation(), locatedException.getMessage(), true);
    }

    public JaninoCompilationProblem(final Location pLocation, final String message, final boolean error) {
      this(pLocation.getFileName(), pLocation, message, error);
    }

    public JaninoCompilationProblem(final String fileName, final String message, final boolean error) {
        this(fileName, null, message, error);
    }

    public JaninoCompilationProblem(final String fileName, final Location location, final String message, final boolean error) {
        this.location = location;
        this.fileName = fileName;
        this.message = message;
        this.error = error;
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
        if (location == null) {
            return 0;
        }
        return location.getLineNumber();
    }

    @Override
    public int getStartColumn() {
        if (location == null) {
            return 0;
        }
        return location.getColumnNumber();
    }

    @Override
    public int getEndLine() {
        return getStartLine();
    }

    @Override
    public int getEndColumn() {
        return getStartColumn();
    }

    @Override
    public String getMessage() {
        return message;
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
