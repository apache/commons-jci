/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jci.stores;

import junit.framework.TestCase;


public final class MemoryResourceStoreTestCase extends TestCase {

    public void testStoring() {
        final ResourceStore store = new MemoryResourceStore();

        final byte[] data = { 1, 2, 3 };
        store.write("key", data);
        
        final byte[] read = store.read("key");
        
        assertTrue(read == data);
        assertTrue(read.equals(data));
    }
    
    public void testRemoving() {
        final ResourceStore store = new MemoryResourceStore();

        final byte[] data = { 1, 2, 3 };
        store.write("key", data);
        
        final byte[] read = store.read("key");
        
        assertTrue(read == data);
        assertTrue(read.equals(data));

        store.remove("key");

        final byte[] empty = store.read("key");
        
        assertTrue(empty == null);
    }
}
