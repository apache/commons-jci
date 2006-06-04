package org.apache.commons.jci.compilers;

import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;

public class JavacClassLoader extends URLClassLoader
{
	private ClassPool classPool;

	private ResourceReader reader;

	private ResourceStore store;

	public JavacClassLoader(ClassPool classPool, ResourceReader reader,
			ResourceStore store, URL[] urls, ClassLoader parent)
	{
		super(urls, parent);
		this.classPool = classPool;
		this.reader = reader;
		this.store = store;
	}

	protected Class findClass(String name) throws ClassNotFoundException
	{
		// System.out.println(name);
		if (name.equals(JavacClassLoader.class.getName()))
		{
			return JavacClassLoader.class;
		}
		if (name.equals(ResourceReader.class.getName()))
		{
			return ResourceReader.class;
		}
		try
		{
			// if (!name.equals(FileInputStreamProxy.class.getName()))
			if (name.equals("com.sun.tools.javac.main.JavaCompiler"))
			{
				CtClass jc = classPool.get(name);
				jc.replaceClassName(FileInputStream.class.getName(),
						FileInputStreamProxy.class.getName());
				return jc.toClass();
			}
		}
		catch (Exception e)
		{
		}
		return super.findClass(name);
	}

	public ResourceReader getReader()
	{
		return reader;
	}

	public ResourceStore getStore()
	{
		return store;
	}
}