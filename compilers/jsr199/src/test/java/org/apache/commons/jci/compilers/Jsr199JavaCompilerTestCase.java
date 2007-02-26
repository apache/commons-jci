package org.apache.commons.jci.compilers;

public final class Jsr199JavaCompilerTestCase extends AbstractCompilerTestCase {

	public String getCompilerName() {
		return "jsr199";
	}
	
	public JavaCompiler createJavaCompiler() {
		return new Jsr199JavaCompiler();
	}

}
