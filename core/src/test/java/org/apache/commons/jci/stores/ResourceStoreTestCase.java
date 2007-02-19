package org.apache.commons.jci.stores;

import org.apache.commons.jci.AbstractTestCase;
import org.apache.commons.lang.ArrayUtils;


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
	}
	
    private void checkReadWrite( final ResourceStore pStore ) {
        final byte[] data = { 1, 2, 3 };
        pStore.write("key", data);
        
        final byte[] read = pStore.read("key");
        
        assertTrue(read != null);
        assertTrue(ArrayUtils.isEquals(data, read));
    }

    private void checkRemove( final ResourceStore pStore ) {
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
