package org.apache.commons.jci.compilers;

public class JavacJavaCompilerTestCase extends AbstractCompilerTestCase {

	public JavaCompiler createJavaCompiler() {
		return new JavacJavaCompiler();
	}

	public String getCompilerName() {
		return "javac";
	}

}
