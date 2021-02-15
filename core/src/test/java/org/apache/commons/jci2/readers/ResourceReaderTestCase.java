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

package org.apache.commons.jci2.readers;

import org.apache.commons.jci2.AbstractTestCase;

/**
 *
 * @author tcurdt
 */
public final class ResourceReaderTestCase extends AbstractTestCase {

    public void testFileResourceReader() throws Exception {
        writeFile("test", "test");
        checkRead(new FileResourceReader(directory));
    }

    public void testMemoryResourceReader() throws Exception {
        final MemoryResourceReader reader = new MemoryResourceReader();
        reader.add("test", "test".getBytes());
        checkRead(reader);
        reader.remove(null);
        assertTrue(reader.isAvailable("test"));
        reader.remove("test");
        assertFalse(reader.isAvailable("test"));
    }

    private void checkRead( final ResourceReader reader ) throws Exception {
        assertTrue(reader.isAvailable("test"));
        final byte[] content = reader.getBytes("test");
        assertTrue(content != null);
        assertEquals("test", new String(content));

        assertFalse(reader.isAvailable("bla"));
        assertTrue(reader.getBytes("bla") == null);
    }
}
