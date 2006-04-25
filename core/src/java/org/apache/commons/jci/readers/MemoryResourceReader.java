package org.apache.commons.jci.readers;

import java.util.Map;
import java.util.HashMap;

public class MemoryResourceReader implements ResourceReader {
    
    private Map files;

    public boolean isAvailable(final String pFileName) {
        if (files == null) {
            return false;
        }

        return files.containsKey( pFileName );
    }
    
    public void addFile(final String pFileName, final char[] pFile) {
        if (files == null) {
            files = new HashMap();
        }
        
        files.put(pFileName, pFile);
    }
    
    public void removeFile(final String pFileName) {
        if (files != null) {
            files.remove(pFileName);
        }    
    }    
    

    public char[] getContent(final String pFileName)
    {
        return (char[]) files.get(pFileName);
    }

    public String[] list() {
        if (files == null) {
            return new String[0];
        }
        return (String[]) files.keySet().toArray(new String[files.size()]);
    }
}
