package org.apache.commons.jci.compilers;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;

public abstract class AbstractCompilerTestCase extends TestCase {

	public abstract JavaCompiler createJavaCompiler();
	
	public abstract String getCompilerName();
	
	public void testFactoryCreation() {
    	final JavaCompiler factoryCompiler = new JavaCompilerFactory().createCompiler(getCompilerName());
    	assertNotNull(factoryCompiler);
    	
    	final JavaCompiler compiler = createJavaCompiler();
    	assertEquals(factoryCompiler.getClass().getName(), compiler.getClass().getName());    	
	}
	
	public void testSimpleCompile() throws Exception {
		final JavaCompiler compiler = createJavaCompiler(); 
		
		final ResourceReader reader = new ResourceReader() {
			final private Map sources = new HashMap() {{
				put("jci/Simple.java", (
					"package jci;\n" +
					"public class Simple {\n" +
					"  public String toString() {\n" +
					"    return \"Simple\";\n" +
					"  }\n" +
					"}").getBytes());
			}};
			
			public byte[] getBytes( final String pResourceName ) {
				return (byte[]) sources.get(pResourceName);
			}

			public boolean isAvailable( final String pResourceName ) {
				return sources.containsKey(pResourceName);
			}
			
		};
		
		final MemoryResourceStore store = new MemoryResourceStore();
		final CompilationResult result = compiler.compile(
				new String[] {
						"jci/Simple.java"
				}, reader, store);
		
		assertEquals(toString(result.getErrors()), 0, result.getErrors().length);		
		assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);
		
		final byte[] clazzBytes = store.read("jci/Simple.class");		
		assertNotNull(clazzBytes);
		assertTrue(clazzBytes.length > 0);
	}

	public void testExtendedCompile() throws Exception {
		final JavaCompiler compiler = createJavaCompiler(); 

		final ResourceReader reader = new ResourceReader() {
			final private Map sources = new HashMap() {{
				put("jci/Simple.java", (
					"package jci;\n" +
					"public class Simple {\n" +
					"  public String toString() {\n" +
					"    return \"Simple\";\n" +
					"  }\n" +
					"}").getBytes());
				put("jci/Extended.java", (
						"package jci;\n" +
						"public class Extended extends Simple {\n" +
						"  public String toString() {\n" +
						"    return \"Extended\" + super.toString();\n" +
						"  }\n" +
						"}").getBytes());
			}};
			
			public byte[] getBytes( final String pResourceName ) {
				return (byte[]) sources.get(pResourceName);
			}

			public boolean isAvailable( final String pResourceName ) {
				return sources.containsKey(pResourceName);
			}
			
		};
		
		final MemoryResourceStore store = new MemoryResourceStore();
		final CompilationResult result = compiler.compile(
				new String[] {
						"jci/Extended.java",
						"jci/Simple.java"
				}, reader, store);
		
		assertEquals(toString(result.getErrors()), 0, result.getErrors().length);		
		assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);
		
		final byte[] clazzBytesSimple = store.read("jci/Simple.class");		
		assertNotNull(clazzBytesSimple);
		assertTrue(clazzBytesSimple.length > 0);

		final byte[] clazzBytesExtended = store.read("jci/Extended.class");
		assertNotNull(clazzBytesExtended);
		assertTrue(clazzBytesExtended.length > 0);
	}

	public void testInternalClassCompile() throws Exception {
		final JavaCompiler compiler = createJavaCompiler(); 
		
		final ResourceReader reader = new ResourceReader() {
			final private Map sources = new HashMap() {{
				put("jci/Simple.java", (
					"package jci;\n" +
					"public class Simple {\n" +
					"  private class Sub {\n" +
					"  }\n" +					
					"  public String toString() {\n" +
					"    new Sub();\n" +
					"    return \"Simple\";\n" +
					"  }\n" +
					"}").getBytes());
			}};
			
			public byte[] getBytes( final String pResourceName ) {
				return (byte[]) sources.get(pResourceName);
			}

			public boolean isAvailable( final String pResourceName ) {
				return sources.containsKey(pResourceName);
			}
			
		};
		
		final MemoryResourceStore store = new MemoryResourceStore();
		final CompilationResult result = compiler.compile(
				new String[] {
						"jci/Simple.java"
				}, reader, store);
		
		assertEquals(toString(result.getErrors()), 0, result.getErrors().length);		
		assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);
		
		final byte[] clazzBytes = store.read("jci/Simple.class");		
		assertNotNull(clazzBytes);
		assertTrue(clazzBytes.length > 0);

		final byte[] subClazzBytes = store.read("jci/Simple$Sub.class");		
		assertNotNull(subClazzBytes);
		assertTrue(subClazzBytes.length > 0);

	}

	public void testUppercasePackageNameCompile() throws Exception {
		final JavaCompiler compiler = createJavaCompiler(); 
		
		final ResourceReader reader = new ResourceReader() {
			final private Map sources = new HashMap() {{
				put("Jci/Simple.java", (
					"package Jci;\n" +
					"public class Simple {\n" +
					"  public String toString() {\n" +
					"    return \"Simple\";\n" +
					"  }\n" +
					"}").getBytes());
			}};
			
			public byte[] getBytes( final String pResourceName ) {
				return (byte[]) sources.get(pResourceName);
			}

			public boolean isAvailable( final String pResourceName ) {
				return sources.containsKey(pResourceName);
			}
			
		};
		
		final MemoryResourceStore store = new MemoryResourceStore();
		final CompilationResult result = compiler.compile(
				new String[] {
						"Jci/Simple.java"
				}, reader, store);
		
		assertEquals(toString(result.getErrors()), 0, result.getErrors().length);		
		assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);
		
		final byte[] clazzBytes = store.read("Jci/Simple.class");		
		assertNotNull(clazzBytes);
		assertTrue(clazzBytes.length > 0);
	}
	
	
	
	public final String toString( final CompilationProblem[] pProblems ) {
		final StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < pProblems.length; i++) {
			final CompilationProblem problem = pProblems[i];
			sb.append(problem.getMessage()).append(", ");
		}
		
		return sb.toString();
	}
	
}
