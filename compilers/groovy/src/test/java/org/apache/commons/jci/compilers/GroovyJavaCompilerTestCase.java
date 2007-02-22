package org.apache.commons.jci.compilers;


public final class GroovyJavaCompilerTestCase extends AbstractCompilerTestCase {

	public String getCompilerName() {
		return "groovy";
	}

	public JavaCompiler createJavaCompiler() {
		return new GroovyJavaCompiler();
	}

	public void testInternalClassCompile() throws Exception {
		// FIXME: inner classes not supported in groovy?
	}

	
}
