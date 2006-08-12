package org.apache.commons.jci.compilers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import org.apache.commons.jci.readers.ResourceReader;

public class FileInputStreamProxy extends InputStream
{
	private InputStream inputStream = null;

	ResourceReader reader = null;

	public FileInputStreamProxy(File file) throws FileNotFoundException
	{
		if (getReader().isAvailable(file.getName()))
			inputStream = new ByteArrayInputStream(getReader().getBytes(file.getName()));
		else
			inputStream = new FileInputStream(file);
	}

	public FileInputStreamProxy(FileDescriptor fdObj)
	{
		inputStream = new FileInputStream(fdObj);
	}

	public FileInputStreamProxy(String name) throws FileNotFoundException
	{
		if (getReader().isAvailable(name))
			inputStream = new ByteArrayInputStream(getReader().getBytes(name));
		else
		inputStream = new FileInputStream(name);
	}

	private ResourceReader getReader()
	{
		if (reader == null)
		{
			JavacClassLoader loader = (JavacClassLoader)Thread.currentThread().getContextClassLoader();
			reader = loader.getReader();
		}
		return reader;
	}

	public int available() throws IOException
	{
		return inputStream.available();
	}

	public void close() throws IOException
	{
		inputStream.close();
	}

	public boolean equals(Object obj)
	{
		return inputStream.equals(obj);
	}

	public FileChannel getChannel()
	{
		// TODO
		throw new RuntimeException(":(");
	}

	public int hashCode()
	{
		return inputStream.hashCode();
	}

	public void mark(int readlimit)
	{
		inputStream.mark(readlimit);
	}

	public boolean markSupported()
	{
		return inputStream.markSupported();
	}

	public int read() throws IOException
	{
		return inputStream.read();
	}

	public int read(byte[] b, int off, int len) throws IOException
	{
		return inputStream.read(b, off, len);
	}

	public int read(byte[] b) throws IOException
	{
		return inputStream.read(b);
	}

	public void reset() throws IOException
	{
		inputStream.reset();
	}

	public long skip(long n) throws IOException
	{
		return inputStream.skip(n);
	}

	public String toString()
	{
		return inputStream.toString();
	}
}
