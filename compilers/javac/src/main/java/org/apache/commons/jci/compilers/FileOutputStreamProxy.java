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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.utils.ConversionUtils;

/**
 * 
 * @author tcurdt
 */
public final class FileOutputStreamProxy extends OutputStream {

    private final static ThreadLocal<ResourceStore> storeThreadLocal = new ThreadLocal<ResourceStore>();

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
        name = ConversionUtils.getResourceNameFromFileName(pName);
    }

    @Override
    public void write(int value) throws IOException {
        out.write(value);
    }

    @Override
    public void close() throws IOException {
        out.close();

        final ResourceStore store = storeThreadLocal.get();

        if (store == null) {
            throw new RuntimeException("forgot to set the ResourceStore for this thread?");
        }

        store.write(name, out.toByteArray());
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }
}
