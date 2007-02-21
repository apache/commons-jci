package org.apache.commons.jci.compilers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;

import junit.framework.TestCase;

public abstract class AbstractCompilerTestCase extends TestCase {

	public abstract JavaCompiler createJavaCompiler();
	
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
		
		assertEquals(0, result.getErrors().length);
		assertEquals(0, result.getWarnings().length);
		
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
						"jci/Simple.java",
						"jci/Extended.java"
				}, reader, store);
		
		assertEquals(0, result.getErrors().length);
		assertEquals(0, result.getWarnings().length);
		
		final byte[] clazzBytesSimple = store.read("jci/Simple.class");		
		assertNotNull(clazzBytesSimple);
		assertTrue(clazzBytesSimple.length > 0);

		final byte[] clazzBytesExtended = store.read("jci/Extended.class");
		assertNotNull(clazzBytesExtended);
		assertTrue(clazzBytesExtended.length > 0);
	}

}
