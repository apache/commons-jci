package org.apache.commons.jci.compilers;

public final class EclipseJavaCompilerTestCase extends AbstractCompilerTestCase {

	public String getCompilerName() {
		return "eclipse";
	}

	public JavaCompiler createJavaCompiler() {
		return new EclipseJavaCompiler();
	}

}
