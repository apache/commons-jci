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
package org.apache.commons.jci.stores;

import org.apache.commons.jci.utils.ConversionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A ClassLoader backed by an array of ResourceStores
 * 
 * @author tcurdt
 */
public final class ResourceStoreClassLoader extends ClassLoader {

    private final Log log = LogFactory.getLog(ResourceStoreClassLoader.class);

    private final ResourceStore[] stores;

    public ResourceStoreClassLoader( final ClassLoader pParent, final ResourceStore[] pStores ) {
        super(pParent);
        
        stores = new ResourceStore[pStores.length]; 
        System.arraycopy(pStores, 0, stores, 0, stores.length);	
    }

    private Class fastFindClass(final String name) {
        
        if (stores != null) {
            for (ResourceStore store : stores) {
                final byte[] clazzBytes = store.read(ConversionUtils.convertClassToResourcePath(name));
                if (clazzBytes != null) {
                    log.debug(getId() + " found class: " + name  + " (" + clazzBytes.length + " bytes)");
                    return defineClass(name, clazzBytes, 0, clazzBytes.length);
                }            
            }
        }
        
        return null;            
    }
    
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // log.debug(getId() + " looking for: " + name);
        Class clazz = findLoadedClass(name);

        if (clazz == null) {
            clazz = fastFindClass(name);
            
            if (clazz == null) {

                final ClassLoader parent = getParent();
                if (parent != null) {
                    clazz = parent.loadClass(name);
                    // log.debug(getId() + " delegating loading to parent: " + name);
                } else {
                    throw new ClassNotFoundException(name);
                }
                
            } else {
                log.debug(getId() + " loaded from store: " + name);
            }
        }

        if (resolve) {
            resolveClass(clazz);
        }

        return clazz;
    }

    protected Class findClass( final String name ) throws ClassNotFoundException {
        final Class clazz = fastFindClass(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        return clazz;
    }
    
    private String getId() {
        return "" + this + "[" + this.getClass().getClassLoader() + "]";
    }
}
