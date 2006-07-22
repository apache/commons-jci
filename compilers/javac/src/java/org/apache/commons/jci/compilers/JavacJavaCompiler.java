package org.apache.commons.jci.compilers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;

public class JavacJavaCompiler extends AbstractJavaCompiler
{
	protected static final String EOL = System.getProperty("line.separator");;

	private static final String WARNING_PREFIX = "warning: ";

	private static final String NOTE_PREFIX = "Note: ";

	private static final String ERROR_PREFIX = "error: ";

	private final JavacJavaCompilerSettings settings;

	public JavacJavaCompiler(final JavacJavaCompilerSettings settings)
	{
		super();
		this.settings = settings;
	}

	public CompilationResult compile(String[] resourcePaths,
			ResourceReader reader, ResourceStore store,
			ClassLoader baseClassLoader)
	{
		ClassPool classPool = ClassPool.getDefault();
		try
		{
			classPool.appendClassPath(System.getProperty("java.home")
					+ "/../lib/tools.jar");
		}
		catch (NotFoundException e)
		{
			return new CompilationResult(
					new CompilationProblem[] { new JavacCompilationProblem(
							"tools.jar not fount", true) });
		}
		JavacClassLoader javacClassLoader = new JavacClassLoader(classPool,
				reader, store, baseClassLoader);
		ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(javacClassLoader);
		try
		{
			Class javacMain = javacClassLoader
					.loadClass("com.sun.tools.javac.Main");
			Method compile = javacMain.getMethod("compile", new Class[] {
					String[].class, PrintWriter.class });
			StringWriter out = new StringWriter();
			Integer ok = (Integer) compile.invoke(null,
					new Object[] { buildCompilerArguments(resourcePaths),
							new PrintWriter(out) });
			CompilationResult result = parseModernStream(new BufferedReader(
					new StringReader(out.toString())));
			if (result.getErrors().length == 0 && ok.intValue() != 0)
				return new CompilationResult(
						new CompilationProblem[] { new JavacCompilationProblem(
								"Failure executing javac, but could not parse the error: "
										+ out.toString(), true) });
			return result;
		}
		catch (Exception e)
		{
			return new CompilationResult(
					new CompilationProblem[] { new JavacCompilationProblem(
							"Error while executing the compiler: "
									+ e.toString(), true) });
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(oldLoader);
		}
	}

	private CompilationResult parseModernStream(BufferedReader input)
			throws IOException
	{
		List problems = new ArrayList();
		String line;
		StringBuffer buffer;
		while (true)
		{
			// cleanup the buffer
			buffer = new StringBuffer(); // this is quicker than clearing it
			// most errors terminate with the '^' char
			do
			{
				line = input.readLine();
				if (line == null)
				{
					return new CompilationResult(
							(CompilationProblem[]) problems
									.toArray(new CompilationProblem[problems
											.size()]));
				}
				// TODO: there should be a better way to parse these
				if (buffer.length() == 0 && line.startsWith(ERROR_PREFIX))
				{
					problems.add(new JavacCompilationProblem(line, true));
				}
				else
					if (buffer.length() == 0 && line.startsWith(NOTE_PREFIX))
					{
						// skip this one - it is JDK 1.5 telling us that the
						// interface is deprecated.
					}
					else
					{
						buffer.append(line);
						buffer.append(EOL);
					}
			}
			while (!line.endsWith("^"));
			// add the error bean
			problems.add(parseModernError(buffer.toString()));
		}
	}

	private CompilationProblem parseModernError(String error)
	{
		StringTokenizer tokens = new StringTokenizer(error, ":");
		boolean isError = true;
		StringBuffer msgBuffer;
		try
		{
			String file = tokens.nextToken();
			// When will this happen?
			if (file.length() == 1)
			{
				file = new StringBuffer(file).append(":").append(
						tokens.nextToken()).toString();
			}
			int line = Integer.parseInt(tokens.nextToken());
			msgBuffer = new StringBuffer();
			String msg = tokens.nextToken(EOL).substring(2);
			isError = !msg.startsWith(WARNING_PREFIX);
			// Remove the 'warning: ' prefix
			if (!isError)
			{
				msg = msg.substring(WARNING_PREFIX.length());
			}
			msgBuffer.append(msg);
			String context = tokens.nextToken(EOL);
			String pointer = tokens.nextToken(EOL);
			if (tokens.hasMoreTokens())
			{
				msgBuffer.append(EOL);
				msgBuffer.append(context); // 'symbol' line
				msgBuffer.append(EOL);
				msgBuffer.append(pointer); // 'location' line
				msgBuffer.append(EOL);
				context = tokens.nextToken(EOL);
				try
				{
					pointer = tokens.nextToken(EOL);
				}
				catch (NoSuchElementException e)
				{
					pointer = context;
					context = null;
				}
			}
			String message = msgBuffer.toString();
			int startcolumn = pointer.indexOf("^");
			int endcolumn = context == null ? startcolumn : context.indexOf(
					" ", startcolumn);
			if (endcolumn == -1)
			{
				endcolumn = context.length();
			}
			return new JavacCompilationProblem(file, isError, line,
					startcolumn, line, endcolumn, message);
		}
		catch (NoSuchElementException e)
		{
			return new JavacCompilationProblem(
					"no more tokens - could not parse error message: " + error,
					isError);
		}
		catch (NumberFormatException e)
		{
			return new JavacCompilationProblem(
					"could not parse error message: " + error, isError);
		}
		catch (Exception e)
		{
			return new JavacCompilationProblem(
					"could not parse error message: " + error, isError);
		}
	}

	private String[] buildCompilerArguments(String[] resourcePaths)
	{
		List args = new ArrayList();
		for (int i = 0; i < resourcePaths.length; i++)
		{
			args.add(resourcePaths[i]);
		}
		if (settings != null)
		{
			if (settings.isOptimize())
				args.add("-O");
			if (settings.isDebug())
				args.add("-g");
			if (settings.isVerbose())
				args.add("-verbose");
			if (settings.isShowDeprecation())
			{
				args.add("-deprecation");
				// This is required to actually display the deprecation messages
				settings.setShowWarnings(true);
			}
			if (settings.getMaxmem() != null)
				args.add("-J-Xmx" + settings.getMaxmem());
			if (settings.getMeminitial() != null)
				args.add("-J-Xms" + settings.getMeminitial());
			if (!settings.isShowWarnings())
				args.add("-nowarn");
			// TODO: this could be much improved
			if (settings.getTargetVersion() != null)
			{
				// Required, or it defaults to the target of your JDK (eg 1.5)
				args.add("-target");
				args.add("1.1");
			}
			else
			{
				args.add("-target");
				args.add(settings.getTargetVersion());
			}
			// TODO suppressSource
			if (settings.getSourceVersion() != null)
			{
				// If omitted, later JDKs complain about a 1.1 target
				args.add("-source");
				args.add("1.3");
			}
			else
			{
				args.add("-source");
				args.add(settings.getSourceVersion());
			}
			// TODO suppressEncoding
			if (settings.getSourceEncoding() != null)
			{
				args.add("-encoding");
				args.add(settings.getSourceEncoding());
			}
			// TODO CustomCompilerArguments
		}
		return (String[]) args.toArray(new String[args.size()]);
	}
}
