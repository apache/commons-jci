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

package org.apache.commons.jci2.utils;

import java.io.File;
import java.util.Locale;

/**
 * Mainly common path manipultation helper methods
 * NOT FOR USE OUTSIDE OF JCI
 *
 * @author tcurdt
 */
public final class ConversionUtils {

    /**
     * Please do not use - internal
     * org/my/Class.xxx -> org.my.Class
     */
    public static String convertResourceToClassName( final String pResourceName ) {
        return ConversionUtils.stripExtension(pResourceName).replace('/', '.');
    }

    /**
     * Please do not use - internal
     * org.my.Class -> org/my/Class.class
     */
    public static String convertClassToResourcePath( final String pName ) {
        return pName.replace('.', '/') + ".class";
    }

    /**
     * Please do not use - internal
     * org/my/Class.xxx -> org/my/Class
     */
    public static String stripExtension( final String pResourceName ) {
        final int i = pResourceName.lastIndexOf('.');
        if (i < 0) {
            return pResourceName;
        }
        final String withoutExtension = pResourceName.substring(0, i);
        return withoutExtension;
    }

    public static String toJavaCasing(final String pName) {
        final char[] name = pName.toLowerCase(Locale.US).toCharArray();
        name[0] = Character.toUpperCase(name[0]);
        return new String(name);
    }

/*
    public static String clazzName( final File base, final File file ) {
        final int rootLength = base.getAbsolutePath().length();
        final String absFileName = file.getAbsolutePath();
        final int p = absFileName.lastIndexOf('.');
        final String relFileName = absFileName.substring(rootLength + 1, p);
        final String clazzName = relFileName.replace(File.separatorChar, '.');
        return clazzName;
    }
*/
    public static String relative( final File base, final File file ) {
        final int rootLength = base.getAbsolutePath().length();
        final String absFileName = file.getAbsolutePath();
        final String relFileName = absFileName.substring(rootLength + 1);
        return relFileName;
    }

    /**
     * a/b/c.java -> a/b/c.java
     * a\b\c.java -> a/b/c.java
     * @param pFileName
     * @return the converted name
     */
    public static String getResourceNameFromFileName( final String pFileName ) {
        if ('/' == File.separatorChar) {
            return pFileName;
        }

        return pFileName.replace(File.separatorChar, '/');
    }

}
