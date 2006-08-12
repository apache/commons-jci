package org.apache.commons.jci.readers;

import org.apache.commons.jci.AbstractTestCase;


public final class FileResourceReaderTestCase extends AbstractTestCase {
    public void testGetContent() throws Exception {
        final ResourceReader reader = new FileResourceReader(directory);
        writeFile("test", "test");

        assertTrue(reader.isAvailable("test"));
        final byte[] content = reader.getBytes("test");
        assertTrue(content != null);
        assertTrue("test".equals(new String(content)));        

        assertTrue(!reader.isAvailable("bla"));
        assertTrue(reader.getBytes("bla") == null);
    }
}
