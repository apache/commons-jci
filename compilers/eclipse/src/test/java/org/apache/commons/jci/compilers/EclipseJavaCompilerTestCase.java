package org.apache.commons.jci.compilers;

public final class EclipseJavaCompilerTestCase extends AbstractCompilerTestCase {

	public JavaCompiler createJavaCompiler() {
		return new EclipseJavaCompiler();
	}

}
