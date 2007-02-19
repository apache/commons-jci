package org.apache.commons.jci.readers;

import org.apache.commons.jci.AbstractTestCase;


public final class ResourceReaderTestCase extends AbstractTestCase {

	public void testFileResourceReader() throws Exception {
        writeFile("test", "test");
		checkRead(new FileResourceReader(directory));
	}

	public void testMemoryResourceReader() throws Exception {
		final MemoryResourceReader reader = new MemoryResourceReader();
		reader.add("test", "test".getBytes());
		checkRead(reader);
	}
	
	private void checkRead( final ResourceReader reader ) throws Exception {
        assertTrue(reader.isAvailable("test"));
        final byte[] content = reader.getBytes("test");
        assertTrue(content != null);
        assertTrue("test".equals(new String(content)));        

        assertTrue(!reader.isAvailable("bla"));
        assertTrue(reader.getBytes("bla") == null);
    }
}
