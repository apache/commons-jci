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

package org.apache.commons.jci2.compiler.eclipse;

import org.apache.commons.jci2.core.problems.CompilationProblem;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Wrapping an Eclipse compiler problem
 */
public final class EclipseCompilationProblem implements CompilationProblem {

    private final IProblem problem;

    public EclipseCompilationProblem(final IProblem problem) {
        this.problem = problem;
    }

    @Override
    public boolean isError() {
        return problem.isError();
    }

    @Override
    public String getFileName() {
        return new String(problem.getOriginatingFileName());
    }

    @Override
    public int getStartLine() {
        return problem.getSourceLineNumber();
    }

    @Override
    public int getStartColumn() {
        return problem.getSourceStart();
    }

    @Override
    public int getEndLine() {
        return getStartLine();
    }

    @Override
    public int getEndColumn() {
        return problem.getSourceEnd();
    }

    @Override
    public String getMessage() {
        return problem.getMessage();
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

    public int getId() {
        return problem.getID();
    }

}
