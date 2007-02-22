package org.apache.commons.jci.compilers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.vafer.dependency.asm.RenamingVisitor;
import org.vafer.dependency.utils.ResourceRenamer;

public final class JavacClassLoader extends ClassLoader {
	
	public JavacClassLoader( final ClassLoader pParent ) {
		super(pParent);
	}
	
	protected Class findClass( final String name ) throws ClassNotFoundException {

		//System.out.println("findClass " + name);
		
		if (name.startsWith("java.")) {
			return super.findClass(name);
		}
		
		final InputStream classStream = getResourceAsStream(name.replace('.', '/') + ".class");
		
		try {
			
			final byte[] classBytes;

			if (name.startsWith("")) {
		        final ClassWriter renamedCw = new ClassWriter(true, false);
		        new ClassReader(classStream).accept(new RenamingVisitor(new CheckClassAdapter(renamedCw), new ResourceRenamer() {
					public String getNewNameFor(final String pOldName) {
						if (pOldName.startsWith(FileOutputStream.class.getName())) {
							//System.out.println("rewriting FOS" + name);
							return FileOutputStreamProxy.class.getName();
						}
						if (pOldName.startsWith(FileInputStream.class.getName())) {
							//System.out.println("rewriting FIS" + name);
							return FileInputStreamProxy.class.getName();
						}
						return pOldName;
					}        		
	        	}), false);

	        	classBytes = renamedCw.toByteArray();
				
			} else {
				classBytes = IOUtils.toByteArray(classStream);						
			}
			
			return defineClass(name, classBytes, 0, classBytes.length);
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