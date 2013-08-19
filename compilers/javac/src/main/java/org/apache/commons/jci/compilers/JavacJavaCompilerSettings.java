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

import java.util.ArrayList;
import java.util.List;

public final class JavacJavaCompilerSettings extends JavaCompilerSettings {

    private boolean optimize;
    private String memMax;
    private String memInitial;
    private String[] customArguments;

    public JavacJavaCompilerSettings() {    	
    }
    
    public JavacJavaCompilerSettings( final JavaCompilerSettings pSettings ) {
    	super(pSettings);
    }
    
    
    public void setCustomArguments( final String[] pCustomArguments ) {
    	customArguments = pCustomArguments;
    }
    
    public String[] getCustomArguments() {
    	return customArguments;
    }
    
    
    public void setMaxMemory( final String pMemMax ) {
    	memMax = pMemMax;
    }
    
    public String getMaxMemory() {
    	return memMax;
    }
    
    
    public void setInitialMemory( final String pMemInitial ) {
    	memInitial = pMemInitial;
    }
    
    public String getInitialMemory() {
    	return memInitial;
    }

    
    public boolean isOptimize() {
        return optimize;
    }

    public void setOptimize( final boolean pOptimize ) {
        optimize = pOptimize;
    }

    
    
    /** @deprecated */
    @Deprecated
    public List<String> getCustomCompilerArguments() {
    	final List<String> list = new ArrayList<String>();
    	for (int i = 0; i < customArguments.length; i++) {
			list.add(customArguments[i]);
		}
    	return list;    	
    }

    /** @deprecated */
    @Deprecated
    public void setCustomCompilerArguments(List<?> customCompilerArguments) {
    	customArguments = customCompilerArguments.toArray(new String[customCompilerArguments.size()]);
    }

    /** @deprecated */
    @Deprecated
    public String getMaxmem() {
        return memMax;
    }

    /** @deprecated */
    @Deprecated
    public void setMaxmem(String maxmem) {
        this.memMax = maxmem;
    }

    /** @deprecated */
    @Deprecated
    public String getMeminitial() {
        return memInitial;
    }

    /** @deprecated */
    @Deprecated
    public void setMeminitial(String meminitial) {
        this.memInitial = meminitial;
    }

    
    
    
    String[] toNativeSettings() {
    	
    	final List<String> args = new ArrayList<String>();

    	if (isOptimize()) {
    		args.add("-O");
    	}

    	if (isDebug()) {
    		args.add("-g");
    	}

    	if (isDeprecations()) {
    		args.add("-deprecation");
    	}

    	if (!isWarnings() && !isDeprecations()) {
    		args.add("-nowarn");
    	}

    	if (getMaxMemory() != null) {
    		args.add("-J-Xmx" + getMaxMemory());
    	}

    	if (getInitialMemory() != null) {
    		args.add("-J-Xms" + getInitialMemory());
    	}

    	args.add("-target");
    	args.add(getTargetVersion());

    	args.add("-source");
    	args.add(getSourceVersion());

    	args.add("-encoding");
    	args.add(getSourceEncoding());

    	if (customArguments != null) {
	    	for (int i = 0; i < customArguments.length; i++) {
				args.add(customArguments[i]);
			}
    	}

    	return args.toArray(new String[args.size()]);
    }
}
