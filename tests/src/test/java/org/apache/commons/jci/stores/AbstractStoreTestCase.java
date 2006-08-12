package org.apache.commons.jci.stores;

import org.apache.commons.jci.AbstractTestCase;
import org.apache.commons.lang.ArrayUtils;


public abstract class AbstractStoreTestCase extends AbstractTestCase {

    protected void testStore(final ResourceStore pStore) {
        final byte[] data = { 1, 2, 3 };
        pStore.write("key", data);
        
        final byte[] read = pStore.read("key");
        
        assertTrue(read != null);
        assertTrue(ArrayUtils.isEquals(data, read));
    }

    protected void testRemove(final ResourceStore pStore) {
        final byte[] data = { 1, 2, 3 };
        pStore.write("key", data);
        
        final byte[] read = pStore.read("key");
        
        assertTrue(read != null);
        assertTrue(ArrayUtils.isEquals(data, read));

        pStore.remove("key");

        final byte[] empty = pStore.read("key");
        
        assertTrue(empty == null);
    }
}
