package org.apache.commons.jci.compilers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class FileOutputStreamProxy extends OutputStream {
	
	
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final String name;	
	
	public FileOutputStreamProxy(File pFile, boolean append) throws FileNotFoundException {
		name = pFile.getName();
	}

	public FileOutputStreamProxy(File pFile) throws FileNotFoundException {
		System.out.println("Writing to file " + pFile);
		name = pFile.getName();
	}

	public FileOutputStreamProxy(FileDescriptor fdObj) {
		throw new RuntimeException();
	}

	public FileOutputStreamProxy(String pName, boolean append) throws FileNotFoundException {
		name = pName;
	}

	public FileOutputStreamProxy(String pName) throws FileNotFoundException {
		name = pName;
	}
	
	public void write(int value) throws IOException {
		out.write(value);
	}

	public void close() throws IOException {
		System.out.println("Wrote " + out.size() + " bytes");
		out.close();
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
