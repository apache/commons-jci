package org.apache.commons.jci.compilers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.jci.readers.ResourceReader;

public class FileInputStreamProxy extends InputStream {
	
	private final static ThreadLocal readerThreadLocal = new ThreadLocal();
//	{
//		public void set(Object o) {
//			System.out.println(this.hashCode() + "@" + this.getClass().getClassLoader().hashCode() + " set " + o + " for " + Thread.currentThread().hashCode());
//			super.set(o);
//			System.out.println(this.hashCode() + "@" + this.getClass().getClassLoader().hashCode() + " get " + super.get() + " for " + Thread.currentThread().hashCode());
//		}
//		
//		public Object get() {
//			final Object o = super.get();
//			System.out.println(this.hashCode() + "@" + this.getClass().getClassLoader().hashCode() + " get " + o + " for " + Thread.currentThread().hashCode());
//			return o;
//		}
//	};
	
	private final InputStream in;
	
	private final String name;
	
	public static void setResourceReader( final ResourceReader pReader ) {
		readerThreadLocal.set(pReader);
	}
	
	public FileInputStreamProxy(File pFile) throws FileNotFoundException {
		this("" + pFile);
	}

	public FileInputStreamProxy(FileDescriptor fdObj) {
		throw new RuntimeException();
	}

	public FileInputStreamProxy(String pName) throws FileNotFoundException {
		name = pName;

		final ResourceReader reader = (ResourceReader) readerThreadLocal.get();

		if (reader == null) {
			throw new RuntimeException("forgot to set the ResourceReader for this thread?");
		}
		
		in = new ByteArrayInputStream(reader.getBytes(name));
	}
	
	public int read() throws IOException {
		return in.read();
	}

	public int available() throws IOException {
		return in.available();			
	}

	public void close() throws IOException {
		in.close();
	}

	public synchronized void mark(int readlimit) {
		in.mark(readlimit);
	}

	public boolean markSupported() {
		return in.markSupported();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	public synchronized void reset() throws IOException {
		in.reset();
	}

	public long skip(long n) throws IOException {
		return in.skip(n);
	}
}
