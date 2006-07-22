package org.apache.commons.jci.compilers;

import org.apache.commons.jci.problems.CompilationProblem;

public class JavacCompilationProblem implements CompilationProblem
{
	private int endCoumn;

	private int endLine;

	private String fileName;

	private String message;

	private int startCoumn;

	private int startLine;

	private boolean isError;

	public JavacCompilationProblem(String message, boolean isError)
	{
		this.message = message;
		this.isError = isError;
		this.fileName = "";
	}

	public JavacCompilationProblem(String fileName, boolean isError,
			int startLine, int startCoumn, int endLine, int endCoumn,
			String message)
	{
		this.message = message;
		this.isError = isError;
		this.fileName = fileName;
		this.startCoumn = startCoumn;
		this.endCoumn = endCoumn;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	public int getEndColumn()
	{
		return endCoumn;
	}

	public int getEndLine()
	{
		return endLine;
	}

	public String getFileName()
	{
		return fileName;
	}

	public String getMessage()
	{
		return message;
	}

	public int getStartColumn()
	{
		return startCoumn;
	}

	public int getStartLine()
	{
		return startLine;
	}

	public boolean isError()
	{
		return isError;
	}

	public String toString()
	{
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
