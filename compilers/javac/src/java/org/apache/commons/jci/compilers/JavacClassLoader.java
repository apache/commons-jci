package org.apache.commons.jci.compilers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;

public class JavacClassLoader extends ClassLoader
{
	private ClassPool classPool;

	private ResourceReader reader;

	private ResourceStore store;

	public JavacClassLoader(ClassPool classPool, ResourceReader reader,
			ResourceStore store, ClassLoader parent)
	{
		super(parent);
		this.classPool = classPool;
		this.reader = reader;
		this.store = store;
	}

	protected Class findClass(String name) throws ClassNotFoundException
	{
		try
		{
			CtClass jc = classPool.get(name);
			if (jc != null)
			{
				if (name.startsWith("com.sun.tools.javac"))
				{
					ClassMap classMap = new ClassMap();
					classMap.put(FileOutputStream.class.getName(),
							FileOutputStreamProxy.class.getName());
					classMap.put(FileInputStream.class.getName(),
							FileInputStreamProxy.class.getName());
					jc.replaceClassName(classMap);
				}
				return jc.toClass();
			}
			else
			{
				return getParent().loadClass(name);
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