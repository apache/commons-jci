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

package org.apache.commons.jci.examples.serverpages;

import java.util.Map;

import org.apache.commons.jci.readers.ResourceReader;

/**
 * @author tcurdt
 */
public final class JspReader implements ResourceReader {

    private final Map<String, byte[]> sources;
    private final ResourceReader reader;


    public JspReader( final Map<String, byte[]> pSources, final ResourceReader pReader ) {
        reader = pReader;
        sources = pSources;
    }


    public byte[] getBytes( String pResourceName ) {

        final byte[] bytes = sources.get(pResourceName);

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
