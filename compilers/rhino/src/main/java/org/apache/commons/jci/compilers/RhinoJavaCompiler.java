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

import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.utils.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author tcurdt
 * based on code from dev.helma.org
 */
public final class RhinoJavaCompiler extends AbstractJavaCompiler {

    private final Log log = LogFactory.getLog(RhinoJavaCompiler.class);

	public CompilationResult compile( final String[] pResourcePaths, final ResourceReader pReader, final ResourceStore pStore, final ClassLoader classLoader) {

		final RhinoCompilingClassLoader cl = new RhinoCompilingClassLoader(pReader, pStore, classLoader);
		
		
		for (int i = 0; i < pResourcePaths.length; i++) {
            log.debug("compiling " + pResourcePaths[i]);
            
            final String clazzName = ClassUtils.convertResourceToClassName(pResourcePaths[i]);
            try {
				cl.loadClass(clazzName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return new CompilationResult(new CompilationProblem[0]);
	}
	
}
