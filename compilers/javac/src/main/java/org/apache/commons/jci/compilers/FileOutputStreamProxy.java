package org.apache.commons.jci.compilers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.jci.stores.ResourceStore;

public class FileOutputStreamProxy extends OutputStream
{
	private ByteArrayOutputStream out = null;

	private ResourceStore store = null;
	
	private String fileName;
	
	public FileOutputStreamProxy(File file, boolean append)
			throws FileNotFoundException
	{
		fileName = file.getName();
		out = new ByteArrayOutputStream();
		if(append)
			try
			{
				out.write(getStore().read(fileName));
			}
			catch (IOException e)
			{
			}
	}

	public FileOutputStreamProxy(File file) throws FileNotFoundException
	{
		fileName = file.getName();
		out = new ByteArrayOutputStream();
	}

	public FileOutputStreamProxy(FileDescriptor fdObj)
	{
		throw new RuntimeException(":(");
	}

	public FileOutputStreamProxy(String name, boolean append)
			throws FileNotFoundException
	{
		fileName = name;
		out = new ByteArrayOutputStream();
		if(append)
			try
			{
				out.write(getStore().read(fileName));
			}
			catch (IOException e)
			{
			}
	}

	public FileOutputStreamProxy(String name) throws FileNotFoundException
	{
		fileName = name;
		out = new ByteArrayOutputStream();
	}
	
	private ResourceStore getStore()
	{
		if (store == null)
		{
			JavacClassLoader loader = (JavacClassLoader)Thread.currentThread().getContextClassLoader();
			store = loader.getStore();
		}
		return store;
	}

	public void close() throws IOException
	{
		getStore().write(fileName, out.toByteArray());
		out.close();
	}

	public boolean equals(Object obj)
	{
		return out.equals(obj);
	}

	public void flush() throws IOException
	{
		out.flush();
	}

	public int hashCode()
	{
		return out.hashCode();
	}

	public String toString()
	{
		return out.toString();
	}

	public void write(byte[] b, int off, int len) throws IOException
	{
		out.write(b, off, len);
	}

	public void write(byte[] b) throws IOException
	{
		out.write(b);
	}

	public void write(int b) throws IOException
	{
		out.write(b);
	}
}
