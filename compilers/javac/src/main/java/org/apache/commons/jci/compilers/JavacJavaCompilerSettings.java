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

package org.apache.commons.jci.compilers;

import java.util.List;

public final class JavacJavaCompilerSettings extends JavaCompilerSettings {

    private boolean optimize;
    private String maxmem;
    private String meminitial;

    private List customCompilerArguments;

    public List getCustomCompilerArguments() {
        return customCompilerArguments;
    }

    public void setCustomCompilerArguments(List customCompilerArguments) {
        this.customCompilerArguments = customCompilerArguments;
    }

    public String getMaxmem() {
        return maxmem;
    }

    public void setMaxmem(String maxmem) {
        this.maxmem = maxmem;
    }

    public String getMeminitial() {
        return meminitial;
    }

    public void setMeminitial(String meminitial) {
        this.meminitial = meminitial;
    }

    public boolean isOptimize() {
        return optimize;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

}
