package org.apache.maven.plugin;

import java.io.File;
import java.util.Collections;
import org.apache.maven.plugin.JCIMojo;
import org.apache.maven.plugin.stubs.DebugEnabledLog;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;

public class JCIMojoTestCase extends AbstractMojoTestCase
{
	protected void setUp() throws Exception
	{
		// required for mojo lookups to work
		super.setUp();
	}

	/**
	 * tests the ability of the plugin to compile a basic file
	 * 
	 * @throws Exception
	 */
	public void testCompilerBasic() throws Exception
	{
		JCIMojo jciMojo = getJCIMojo("target/test-classes/unit/compiler-basic-test/plugin-config.xml");
		jciMojo.execute();
		File testClass = new File(jciMojo.getOutputDirectory(),
				"TestCompile0.class");
		assertTrue(testClass.exists());
	}

	private JCIMojo getJCIMojo(String pomXml) throws Exception
	{
		File testPom = new File(getBasedir(), pomXml);
		assertTrue(testPom.exists());
		JCIMojo mojo = (JCIMojo) lookupMojo("compile", testPom);
		setVariableValueToObject(mojo, "log", new DebugEnabledLog());
		setVariableValueToObject(mojo, "projectArtifact", new ArtifactStub());
		setVariableValueToObject(mojo, "classpathElements",
				Collections.EMPTY_LIST);
		assertNotNull(mojo);
		return mojo;
	}
}
package org.apache.maven.plugin;

import java.io.File;
import java.util.Collections;
import org.apache.maven.plugin.JCIMojo;
import org.apache.maven.plugin.stubs.DebugEnabledLog;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;

public class JCIMojoTestCase extends AbstractMojoTestCase
{
	protected void setUp() throws Exception
	{
		// required for mojo lookups to work
		super.setUp();
	}

	/**
	 * tests the ability of the plugin to compile a basic file
	 * 
	 * @throws Exception
	 */
	public void testCompilerBasic() throws Exception
	{
		JCIMojo jciMojo = getJCIMojo("target/test-classes/unit/compiler-basic-test/plugin-config.xml");
		jciMojo.execute();
		File testClass = new File(jciMojo.getOutputDirectory(),
				"TestCompile0.class");
		assertTrue(testClass.exists());
	}

	private JCIMojo getJCIMojo(String pomXml) throws Exception
	{
		File testPom = new File(getBasedir(), pomXml);
		assertTrue(testPom.exists());
		JCIMojo mojo = (JCIMojo) lookupMojo("compile", testPom);
		setVariableValueToObject(mojo, "log", new DebugEnabledLog());
		setVariableValueToObject(mojo, "projectArtifact", new ArtifactStub());
		setVariableValueToObject(mojo, "classpathElements",
				Collections.EMPTY_LIST);
		assertNotNull(mojo);
		return mojo;
	}
}
