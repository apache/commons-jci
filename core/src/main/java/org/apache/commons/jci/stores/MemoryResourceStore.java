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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author tcurdt
 */
public final class MemoryResourceStore implements ResourceStore {

    private final Log log = LogFactory.getLog(MemoryResourceStore.class);

	private final Map store = new HashMap();
	
	public byte[] read( final String pResourceName ) {
		return (byte[]) store.get(pResourceName);
	}

	public void write( final String pResourceName, final byte[] pData ) {
		log.debug("storing resource " + pResourceName + "(" + pData.length + ")");
		store.put(pResourceName, pData);
	}
	
    public void remove( final String pResourceName ) {
        log.debug("removing resource " + pResourceName);
        store.remove(pResourceName);
    }

    /**
     * @deprecated
     */
    public String[] list() {
        if (store == null) {
            return new String[0];
        }
        final List names = new ArrayList();
        
        for (final Iterator it = store.keySet().iterator(); it.hasNext();) {
            final String name = (String) it.next();
            names.add(name);
        }

        return (String[]) names.toArray(new String[store.size()]);
    }
    
    public String toString() {
        return this.getClass().getName() + store.toString();
    }
}
