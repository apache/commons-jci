/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.jci2.core.stores;

/**
 * A TransactionalResourceStore get signals of the compilation process as a whole.
 * When it started and when the compiler finished.
 */
public class TransactionalResourceStore implements ResourceStore, Transactional {

    private final ResourceStore store;

    public TransactionalResourceStore( final ResourceStore pStore ) {
        store = pStore;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public byte[] read( final String pResourceName ) {
        return store.read(pResourceName);
    }
    @Override
    public void remove( final String pResourceName ) {
        store.remove(pResourceName);
    }
    @Override
    public void write( final String pResourceName, final byte[] pResourceData ) {
        store.write(pResourceName, pResourceData);
    }

    @Override
    public String toString() {
        return store.toString();
    }
}
