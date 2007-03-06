package org.apache.commons.jci.compilers;

public class JavacJavaCompilerTestCase extends AbstractCompilerTestCase {

	public JavaCompiler createJavaCompiler() {
		return new JavacJavaCompiler();
	}

	public String getCompilerName() {
		return "javac";
	}

	public void testForToolsJar() {
		try {
			Class.forName("com.sun.tools.javac.Main");
		} catch (ClassNotFoundException e) {
			final StringBuffer sb = new StringBuffer();
			sb.append("Could not find javac compiler class (should be in the tools.jar/classes.jar in your JRE/JDK). ");
			sb.append("os.name").append('=').append(System.getProperty("os.name")).append(", ");
			sb.append("os.version").append('=').append(System.getProperty("os.version")).append(", ");
			sb.append("java.class.path").append('=').append(System.getProperty("java.class.path"));
			fail(sb.toString());
		}
	}
}
