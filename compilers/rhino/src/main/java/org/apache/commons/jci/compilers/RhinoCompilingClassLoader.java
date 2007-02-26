package org.apache.commons.jci.compilers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.GeneratedClassLoader;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.optimizer.ClassCompiler;

/**
 * @author tcurdt
 * based on code from dev.helma.org
 * http://dev.helma.org/source/file/helma/branches/rhinoloader/src/org/helma/javascript/RhinoLoader.java/?revision=95
 */

public final class RhinoCompilingClassLoader extends ClassLoader {


	File scriptDir;
	private final ScriptableObject scope;
	private final ResourceReader reader;
	private final ResourceStore store;

	public RhinoCompilingClassLoader( final ResourceReader pReader, final ResourceStore pStore, final ClassLoader pClassLoader) {
		super(pClassLoader);
		
		reader = pReader;
		store = pStore;

		final Context cx = Context.enter();
		scope = new ImporterTopLevel(cx);
		Context.exit();
	}



	/**
	 * Find and load a java class implemented in JavaScript.
	 * @param name the class name
	 * @return the class
	 * @throws ClassNotFoundException
	 * @throws JavaScriptException
	 */

	protected Class findClass(String name) throws ClassNotFoundException {

		final Context cx = Context.enter();

		try {
			return compileClass(cx, name);
		} catch (IOException iox) {
			throw new ClassNotFoundException(iox.getMessage(), iox);
		} finally {
			Context.exit();
		}
	}



	/**
	 *
	 * @param cx the rhino context
	 * @param className the class name
	 * @return the compiled class
	 * @throws IOException an i/o related error occurred
	 * @throws ClassNotFoundException class couldn't be found
	 */

	private Class compileClass(Context cx, String className) throws IOException, ClassNotFoundException {

		Class superclass = null;
		Class[] interfaces = null;

		String file = className.replace('.', '/') + ".js";
		
		Scriptable target = evaluate(cx, file);

		// first get the base class

		Object ext = ScriptableObject.getProperty(target, "__extends__");

		if (ext instanceof String) {
			superclass = Class.forName((String) ext);
		}

		// then add the implemented interfaces

		ArrayList list = new ArrayList();

		Object impl = ScriptableObject.getProperty(target, "__implements__");

		if (impl instanceof NativeArray) {

			NativeArray array = (NativeArray) impl;

			for (int i=0; i<array.getLength(); i++) {

				Object obj = array.get(i, array);

				if (obj instanceof String) {

					list.add(Class.forName((String) obj));

				}

			}

		} else if (impl instanceof String) {

			list.add(Class.forName((String) impl));

		}


		if (!list.isEmpty()) {

			interfaces = new Class[list.size()];

			interfaces = (Class[]) list.toArray(interfaces);

		}

		return compileClass(cx, file, className, superclass, interfaces);

	}



	/**
	 *
	 * @param cx the rhino context
	 * @param file the file to read fom
	 * @param className the class name
	 * @param superclass the super class
	 * @param interfaces the implemented interfaces
	 * @return the compiled class
	 * @throws IOException an i/o related error occured
	 */

	private Class compileClass(Context cx, String file, String className, Class superclass, Class[] interfaces) throws IOException {

		CompilerEnvirons compEnv = new CompilerEnvirons();
		compEnv.initFromContext(cx);
		ClassCompiler compiler = new ClassCompiler(compEnv);

		if (superclass != null) {
			compiler.setTargetExtends(superclass);
		}

		if (interfaces != null) {
			compiler.setTargetImplements(interfaces);
		}


		byte[] buf = reader.getBytes(file);

		Object[] classes = compiler.compileToClassFiles(new String(buf), getName(file), 1, className);

		GeneratedClassLoader loader = cx.createClassLoader(cx.getApplicationClassLoader());

		Class clazz = null;

		for (int i = 0; i < classes.length; i += 2) {

			final String clazzName = (String) classes[i];
			final byte[] clazzBytes = (byte[]) classes[i+1];
			
			store.write(clazzName.replace('.', '/') + ".class", clazzBytes);
			
			Class c = loader.defineClass(clazzName, clazzBytes);
			loader.linkClass(c);

			if (i == 0) {
				clazz = c;
			}

		}

		return clazz;
	}

	private String getName(String s) {
		final int i = s.lastIndexOf('/');
		if (i < 0) {
			return s;
		}
		
		return s.substring(i + 1);
	}
	
	/**
	 * Evaluate the script on a new Scriptable object.
	 *
	 * @param cx the current Context
	 * @param file the file to evaluate
	 * @return the scriptable object on which the script was evaluated
	 * @throws JavaScriptException if an error occurred evaluating the
	 *                             prototype script code
	 * @throws IOException if an error occurred reading the script file
	 */

	private Scriptable evaluate(Context cx, String sourcePath) throws JavaScriptException, IOException {

		if (!reader.isAvailable(sourcePath)) {
			throw new FileNotFoundException("File " + sourcePath + " not found or not readable");
		}

		final Scriptable target = cx.newObject(scope);

		final byte[] script = reader.getBytes(sourcePath);
		
		Reader reader = new InputStreamReader(new ByteArrayInputStream(script));

		System.out.println(new String(script));
		
		cx.evaluateReader(target, reader, getName(sourcePath), 1, null);

		return target;
	}

}