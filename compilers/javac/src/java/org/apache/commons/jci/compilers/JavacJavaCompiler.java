package org.apache.commons.jci.compilers;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.lang.ArrayUtils;

public class JavacJavaCompiler extends AbstractJavaCompiler
{
	public CompilationResult compile(String[] resourcePaths,
			ResourceReader reader, ResourceStore store,
			ClassLoader baseClassLoader)
	{
		File toolsJar = new File(System.getProperty("java.home"),
				"../lib/tools.jar");
		URL[] urls = null;
		try
		{
			if (baseClassLoader instanceof URLClassLoader)
			{
				urls = (URL[]) ArrayUtils.add(
						((URLClassLoader) baseClassLoader).getURLs(), toolsJar
								.toURI().toURL());
			}
			else
			{
				urls = new URL[] { new File(".").toURI().toURL(),
						toolsJar.toURL() };
			}
		}
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ClassPool classPool = ClassPool.getDefault();
		JavacClassLoader javacClassLoader = new JavacClassLoader(classPool,
				reader, store, urls, baseClassLoader.getParent());
		ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(javacClassLoader);
		Class javacMain = null;
		try
		{
			javacMain = javacClassLoader.loadClass("com.sun.tools.javac.Main");
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		Method compile = null;
		try
		{
			compile = javacMain.getMethod("compile", new Class[] {
					String[].class, PrintWriter.class });
		}
		catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		try
		{
			compile.invoke(null, new Object[] { new String[] { "test.java" },
					new PrintWriter(System.out) });
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread.currentThread().setContextClassLoader(oldLoader);
		return null;
	}
}
