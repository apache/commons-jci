package org.apache.commons.jci.compilers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;

public final class RhinoJavaCompilerTestCase extends AbstractCompilerTestCase {

	public JavaCompiler createJavaCompiler() {
		return new RhinoJavaCompiler();
	}
	
	public String getCompilerName() {
		return "rhino";
	}
	
	public void testSimpleCompile() throws Exception {
		final JavaCompiler compiler = createJavaCompiler(); 
		
		final ResourceReader reader = new ResourceReader() {
			final private Map sources = new HashMap() {{
				put("jci/Simple.js", (
					" var i = 0;\n" +
					"\n"
					).getBytes());
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
						"jci/Simple.js"
				}, reader, store);
		
		assertEquals(toString(result.getErrors()), 0, result.getErrors().length);		
		assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);
		
		final byte[] clazzBytes = store.read("jci/Simple.class");		
		assertNotNull(clazzBytes);
		assertTrue(clazzBytes.length > 0);
	}

	public void testExtendedCompile() throws Exception {
	}

	public void testInternalClassCompile() throws Exception {
	}

	public void testUppercasePackageNameCompile() throws Exception {
		final JavaCompiler compiler = createJavaCompiler(); 
		
		final ResourceReader reader = new ResourceReader() {
			final private Map sources = new HashMap() {{
				put("Jci/Simple.js", (
					" var i = 0;\n" +
					"\n"
					).getBytes());
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
						"Jci/Simple.js"
				}, reader, store);
		
		assertEquals(toString(result.getErrors()), 0, result.getErrors().length);		
		assertEquals(toString(result.getWarnings()), 0, result.getWarnings().length);
		
		final byte[] clazzBytes = store.read("Jci/Simple.class");		
		assertNotNull(clazzBytes);
		assertTrue(clazzBytes.length > 0);
	}
	
	

}
