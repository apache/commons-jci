package org.apache.commons.jci.compilers;

import java.util.List;

public class JavacJavaCompilerSettings extends JavaCompilerSettings
{
	private boolean optimize;

	private boolean debug;

	private boolean verbose;

	private boolean showDeprecation;

	private String maxmem;

	private String meminitial;

	private boolean showWarnings;

	private String targetVersion;

	private String sourceVersion;

	private String sourceEncoding;

	private List customCompilerArguments;

	public List getCustomCompilerArguments()
	{
		return customCompilerArguments;
	}

	public void setCustomCompilerArguments(List customCompilerArguments)
	{
		this.customCompilerArguments = customCompilerArguments;
	}

	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public boolean isShowDeprecation()
	{
		return showDeprecation;
	}

	public void setShowDeprecation(boolean deprecation)
	{
		this.showDeprecation = deprecation;
	}

	public String getMaxmem()
	{
		return maxmem;
	}

	public void setMaxmem(String maxmem)
	{
		this.maxmem = maxmem;
	}

	public String getMeminitial()
	{
		return meminitial;
	}

	public void setMeminitial(String meminitial)
	{
		this.meminitial = meminitial;
	}

	public boolean isOptimize()
	{
		return optimize;
	}

	public void setOptimize(boolean optimize)
	{
		this.optimize = optimize;
	}

	public boolean isShowWarnings()
	{
		return showWarnings;
	}

	public void setShowWarnings(boolean showWarnings)
	{
		this.showWarnings = showWarnings;
	}

	public String getSourceEncoding()
	{
		return sourceEncoding;
	}

	public void setSourceEncoding(String sourceEncoding)
	{
		this.sourceEncoding = sourceEncoding;
	}

	public String getSourceVersion()
	{
		return sourceVersion;
	}

	public void setSourceVersion(String sourceVersion)
	{
		this.sourceVersion = sourceVersion;
	}

	public String getTargetVersion()
	{
		return targetVersion;
	}

	public void setTargetVersion(String targetVersion)
	{
		this.targetVersion = targetVersion;
	}

	public boolean isVerbose()
	{
		return verbose;
	}

	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}
}
