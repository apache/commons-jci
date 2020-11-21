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

package org.apache.commons.jci2.stores;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Store just in memory
 * 
 * @author tcurdt
 */
public final class MemoryResourceStore implements ResourceStore {

    private final Log log = LogFactory.getLog(MemoryResourceStore.class);

    private final Map<String, byte[]> store = new HashMap<String, byte[]>();

    public byte[] read( final String pResourceName ) {
        log.debug("reading resource " + pResourceName);
        return store.get(pResourceName);
    }

    public void write( final String pResourceName, final byte[] pData ) {
        log.debug("writing resource " + pResourceName + "(" + pData.length + ")");
        store.put(pResourceName, pData);
    }

    public void remove( final String pResourceName ) {
        log.debug("removing resource " + pResourceName);
        store.remove(pResourceName);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String[] list() {
        if (store == null) {
            return new String[0];
        }
        final List<String> names = new ArrayList<String>();
        
        for (final String name : store.keySet()) {
            names.add(name);
        }

        return names.toArray(new String[store.size()]);
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + store.toString();
    }
}
