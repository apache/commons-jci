package org.apache.maven.plugin;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;

/**
 * Compiles application sources
 * 
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution compile
 */
public class JCIMojo extends AbstractJCIMojo
{
	/**
	 * The source directories containing the sources to be compiled.
	 * 
	 * @parameter expression="${project.compileSourceRoots}"
	 * @required
	 * @readonly
	 */
	private List compileSourceRoots;

	/**
	 * Project classpath.
	 * 
	 * @parameter expression="${project.compileClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List classpathElements;

	/**
	 * The directory for compiled classes.
	 * 
	 * @parameter expression="${project.build.outputDirectory}"
	 * @required
	 * @readonly
	 */
	private File outputDirectory;

	/**
	 * Project artifacts.
	 * 
	 * @parameter expression="${project.artifact}"
	 * @required
	 * @readonly
	 * @todo this is an export variable, really
	 */
	private Artifact projectArtifact;

	/**
	 * A list of inclusion filters for the compiler.
	 * 
	 * @parameter
	 */
	private Set includes = new HashSet();

	/**
	 * A list of exclusion filters for the compiler.
	 * 
	 * @parameter
	 */
	private Set excludes = new HashSet();

	protected List getCompileSourceRoots()
	{
		return compileSourceRoots;
	}

	protected List getClasspathElements()
	{
		return classpathElements;
	}

	protected File getOutputDirectory()
	{
		return outputDirectory;
	}

	public void execute() throws MojoExecutionException, MojoFailureException
	{
		super.execute();
		projectArtifact.setFile(outputDirectory);
	}
}
