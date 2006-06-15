package org.apache.maven.plugin;

import java.io.File;
import java.util.List;
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
	 *            default-value="eclipse"
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
		JavaCompiler compiler = JavaCompilerFactory.getInstance()
				.createCompiler(compilerId);
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
		FileResourceReader fileResourceReader = new FileResourceReader(
				new File((String) getCompileSourceRoots().get(0)));
		FileResourceStore fileResourceStore = new FileResourceStore(
				getOutputDirectory());
		compiler.compile(new String[] { "TestCompile0.java" },
				fileResourceReader, fileResourceStore);
	}
}
