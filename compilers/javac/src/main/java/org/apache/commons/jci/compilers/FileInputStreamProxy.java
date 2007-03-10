/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.jci.compilers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.utils.ConversionUtils;

/**
 * 
 * @author tcurdt
 */
public final class FileInputStreamProxy extends InputStream {
	
	private final static ThreadLocal readerThreadLocal = new ThreadLocal();
	
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
		name = ConversionUtils.getResourceNameFromFileName(pName);

		final ResourceReader reader = (ResourceReader) readerThreadLocal.get();

		if (reader == null) {
			throw new RuntimeException("forgot to set the ResourceReader for this thread?");
		}
		
		final byte[] bytes = reader.getBytes(name);
		
		if (bytes == null) {
			throw new FileNotFoundException(name);
		}
		
		in = new ByteArrayInputStream(bytes);
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
