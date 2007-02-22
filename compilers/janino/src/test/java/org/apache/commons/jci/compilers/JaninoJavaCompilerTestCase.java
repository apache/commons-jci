package org.apache.commons.jci.compilers;

public final class JaninoJavaCompilerTestCase extends AbstractCompilerTestCase {

	public String getCompilerName() {
		return "janino";
	}
	
	public JavaCompiler createJavaCompiler() {
		return new JaninoJavaCompiler();
	}

}
