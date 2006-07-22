package org.apache.maven.plugin;

import java.io.File;
import java.util.List;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.readers.FileResourceReader;
import org.apache.commons.jci.stores.FileResourceStore;

public abstract class AbstractJCIMojo extends AbstractMojo
{
	/**
	 * Whether to include debugging information in the compiled class files. The
	 * default value is true.
	 * 
	 * @parameter expression="${maven.compiler.debug}" default-value="true"
	 */
	private boolean debug;

	/**
	 * The compiler id of the compiler to use.
	 * 
	 * @parameter expression="${maven.compiler.compilerId}"
	 *            default-value="javac"
	 */
	private String compilerId;

	/**
	 * The directory to run the compiler from if fork is true.
	 * 
	 * @parameter expression="${basedir}"
	 * @required
	 * @readonly
	 */
	private File basedir;

	/**
	 * The target directory of the compiler if fork is true.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 * @readonly
	 */
	private File buildDirectory;

	protected abstract List getClasspathElements();

	protected abstract List getCompileSourceRoots();

	protected abstract File getOutputDirectory();

	public void execute() throws MojoExecutionException, MojoFailureException
	{
		getLog().debug(compilerId);
		JavaCompiler compiler = JavaCompilerFactory.getInstance()
				.createCompiler(compilerId);
		logSettings();
		FileResourceReader fileResourceReader = new FileResourceReader(
				new File((String) getCompileSourceRoots().get(0)));
		getOutputDirectory().mkdirs();
		FileResourceStore fileResourceStore = new FileResourceStore(
				getOutputDirectory());
		CompilationResult result = compiler.compile(
				new String[] { "TestCompile0.java" }, fileResourceReader,
				fileResourceStore);
		logResult(result);
	}

	private void logSettings()
	{
		if (getLog().isDebugEnabled())
		{
			getLog().debug(
					"Source directories: "
							+ getCompileSourceRoots().toString().replace(',',
									'\n'));
			getLog().debug(
					"Classpath: "
							+ getClasspathElements().toString().replace(',',
									'\n'));
			getLog().debug("Output directory: " + getOutputDirectory());
		}
	}

	protected void logResult(CompilationResult result)
	{
		for (int i = 0; i < result.getErrors().length; i++)
		{
			getLog().error(result.getErrors()[i].toString());
		}
		for (int i = 0; i < result.getWarnings().length; i++)
		{
			getLog().warn(result.getWarnings()[i].toString());
		}
	}
}
