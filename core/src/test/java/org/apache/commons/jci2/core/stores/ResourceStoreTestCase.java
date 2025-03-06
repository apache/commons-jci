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

package org.apache.commons.jci2.core.stores;

import org.apache.commons.jci2.core.AbstractTestCase;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author tcurdt
 */
public final class ResourceStoreTestCase extends AbstractTestCase {

    public void testMemoryResourceStore() {
        checkReadWrite(new MemoryResourceStore());
        checkRemove(new MemoryResourceStore());
    }

    public void testFileResourceStore() {
        checkReadWrite(new FileResourceStore(directory));
        checkRemove(new FileResourceStore(directory));
    }

    public void testTransactionalFileResourceStore() {
        checkReadWrite(new TransactionalResourceStore(new FileResourceStore(directory)));
        checkRemove(new TransactionalResourceStore(new FileResourceStore(directory)));

        final ResourceStore rs = new FileResourceStore(directory);
        final TransactionalResourceStore trs = new TransactionalResourceStore(rs);
        assertEquals(rs.toString(), trs.toString());
    }

    private void checkReadWrite( final ResourceStore pStore ) {
        final byte[] data = { 1, 2, 3 };
        pStore.write("key", data);

        final byte[] read = pStore.read("key");

        assertNotNull(read);
        assertTrue(ArrayUtils.isEquals(data, read));
    }

    private void checkRemove( final ResourceStore pStore ) {
        final byte[] data = { 1, 2, 3 };
        pStore.write("key", data);

        final byte[] read = pStore.read("key");

        assertNotNull(read);
        assertTrue(ArrayUtils.isEquals(data, read));

        pStore.remove("key");

        final byte[] empty = pStore.read("key");

        assertNull(empty);
    }
}
