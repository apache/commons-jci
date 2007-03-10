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

	private int endCoumn;
	private int endLine;
	private String fileName;
	private String message;
	private int startCoumn;
	private int startLine;
	private boolean isError;

	public JavacCompilationProblem(String message, boolean isError) {
		this.message = message;
		this.isError = isError;
		this.fileName = "";
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

	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append(getFileName()).append(" (");
		sb.append(getStartLine());
		sb.append(":");
		sb.append(getStartColumn());
		sb.append(") : ");
		sb.append(getMessage());
		return sb.toString();
	}
}
