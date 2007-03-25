package org.apache.commons.jci.examples.serverpages;

import java.util.Map;

import org.apache.commons.jci.readers.ResourceReader;

public final class JspReader implements ResourceReader {

	private final Map sources;
	private final ResourceReader reader;
	
	
	public JspReader( final Map pSources, final ResourceReader pReader ) {
		reader = pReader;
		sources = pSources;
	}
	
	
	public byte[] getBytes( String pResourceName ) {

		final byte[] bytes = (byte[]) sources.get(pResourceName);
		
		if (bytes != null) {
			return bytes;
		}

		return reader.getBytes(pResourceName);		
	}

	public boolean isAvailable( String pResourceName ) {

		if (sources.containsKey(pResourceName)) {
			return true;
		}
		
		return reader.isAvailable(pResourceName);
	}

}
