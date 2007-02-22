package org.apache.commons.jci.compilers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.jci.stores.ResourceStore;

public class FileOutputStreamProxy extends OutputStream {
	
	private final static ThreadLocal storeThreadLocal = new ThreadLocal();

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final String name;	
	
	
	public static void setResourceStore( final ResourceStore pStore ) {
		storeThreadLocal.set(pStore);
	}

	
	public FileOutputStreamProxy(File pFile, boolean append) throws FileNotFoundException {
		this("" + pFile);
	}

	public FileOutputStreamProxy(File pFile) throws FileNotFoundException {
		this("" + pFile);
	}

	public FileOutputStreamProxy(FileDescriptor fdObj) {
		throw new RuntimeException();
	}

	public FileOutputStreamProxy(String pName, boolean append) throws FileNotFoundException {
		this(pName);
	}

	public FileOutputStreamProxy(String pName) throws FileNotFoundException {
		name = pName;
	}
	
	public void write(int value) throws IOException {
		out.write(value);
	}

	public void close() throws IOException {
		out.close();
		
		final ResourceStore store = (ResourceStore) storeThreadLocal.get();

		if (store == null) {
			throw new RuntimeException("forgot to set the ResourceStore for this thread?");
		}
		
		store.write(name, out.toByteArray());
	}

	public void flush() throws IOException {
		out.flush();
	}

	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	public void write(byte[] b) throws IOException {
		out.write(b);
	}
}
