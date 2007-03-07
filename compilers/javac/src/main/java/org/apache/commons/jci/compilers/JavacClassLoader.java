package org.apache.commons.jci.compilers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.vafer.dependency.asm.RenamingVisitor;
import org.vafer.dependency.utils.ResourceRenamer;

public final class JavacClassLoader extends URLClassLoader {
	
	
	private static URL[] getToolsJar() {
		try {
			Class.forName("com.sun.tools.javac.Main");
			
			// found - no addtional classpath entry required
			return new URL[0];

		} catch (Exception e) {
		}

		// no compiler in current classpath, let's try to find the tools.jar

		String javaHome = System.getProperty("java.home");
		if (javaHome.toLowerCase(Locale.US).endsWith(File.separator + "jre")) {
			javaHome = javaHome.substring(0, javaHome.length()-4);
		}
		
		final File toolsJar = new File(javaHome + "/lib/tools.jar");

		if (toolsJar.exists()) {
			try {
				return new URL[] { toolsJar.toURL() };
			} catch (MalformedURLException e) {
			}
		}
		
		final StringBuffer sb = new StringBuffer();
		sb.append("Could not find javac compiler class (should be in the tools.jar/classes.jar in your JRE/JDK). ");
		sb.append("os.name").append('=').append(System.getProperty("os.name")).append(", ");
		sb.append("os.version").append('=').append(System.getProperty("os.version")).append(", ");
		sb.append("java.class.path").append('=').append(System.getProperty("java.class.path"));

		throw new RuntimeException(sb.toString());
	}
	
	private final Map loaded = new HashMap();
	
	public JavacClassLoader( final ClassLoader pParent ) {
		super(getToolsJar(), pParent);
	}
	
	protected Class findClass( final String name ) throws ClassNotFoundException {

		//System.out.println("findClass " + name);
		
		if (name.startsWith("java.")) {
			return super.findClass(name);
		}
		
		try {

			final Class clazz = (Class) loaded.get(name);
			if (clazz != null) {
				return clazz;
			}
						
			final byte[] classBytes;

			if (name.startsWith("com.sun.tools.javac.")) {
				final InputStream classStream = getResourceAsStream(name.replace('.', '/') + ".class");
				
		        final ClassWriter renamedCw = new ClassWriter(true, false);
		        new ClassReader(classStream).accept(new RenamingVisitor(new CheckClassAdapter(renamedCw), new ResourceRenamer() {
					public String getNewNameFor(final String pOldName) {
						if (pOldName.startsWith(FileOutputStream.class.getName())) {
//							System.out.println("rewriting FOS " + name);
							return FileOutputStreamProxy.class.getName();
						}
						if (pOldName.startsWith(FileInputStream.class.getName())) {
//							System.out.println("rewriting FIS " + name);
							return FileInputStreamProxy.class.getName();
						}
						return pOldName;
					}        		
	        	}), false);

	        	classBytes = renamedCw.toByteArray();
				
			} else {
//				classBytes = IOUtils.toByteArray(classStream);
				return super.findClass(name);
			}
			
			final Class newClazz = defineClass(name, classBytes, 0, classBytes.length);			
			loaded.put(name, newClazz);			
			return newClazz;
		} catch (IOException e) {
			throw new ClassNotFoundException("", e);
		}
	}

	protected synchronized Class loadClass( final String classname, final boolean resolve ) throws ClassNotFoundException {

		Class theClass = findLoadedClass(classname);
		if (theClass != null) {
			return theClass;
		}

		try {
			theClass = findClass(classname);
		} catch (ClassNotFoundException cnfe) {
			theClass = getParent().loadClass(classname);
		}

		if (resolve) {
			resolveClass(theClass);
		}

		return theClass;
	}
}