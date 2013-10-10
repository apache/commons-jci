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

package org.apache.commons.jci2.compilers;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.TestCase;

public final class EclipseJavaCompilerSettingsTestCase extends TestCase {

	public void testDefaultSettings() {
		final Map<String, String> m = new EclipseJavaCompilerSettings().toNativeSettings();
		assertEquals(CompilerOptions.DO_NOT_GENERATE, m.get(CompilerOptions.OPTION_SuppressWarnings));
		assertEquals(CompilerOptions.DO_NOT_GENERATE, m.get(CompilerOptions.OPTION_ReportDeprecation));
		assertEquals(CompilerOptions.VERSION_1_4, m.get(CompilerOptions.OPTION_TargetPlatform));
		assertEquals(CompilerOptions.VERSION_1_4, m.get(CompilerOptions.OPTION_Source));
		assertEquals("UTF-8", m.get(CompilerOptions.OPTION_Encoding));
	}

	public void testSourceVersion() {
		final EclipseJavaCompilerSettings s = new EclipseJavaCompilerSettings();
		s.setSourceVersion("1.1");
		assertEquals(CompilerOptions.VERSION_1_1, s.toNativeSettings().get(CompilerOptions.OPTION_Source));
		s.setSourceVersion("1.2");
		assertEquals(CompilerOptions.VERSION_1_2, s.toNativeSettings().get(CompilerOptions.OPTION_Source));
		s.setSourceVersion("1.3");
		assertEquals(CompilerOptions.VERSION_1_3, s.toNativeSettings().get(CompilerOptions.OPTION_Source));
		s.setSourceVersion("1.4");
		assertEquals(CompilerOptions.VERSION_1_4, s.toNativeSettings().get(CompilerOptions.OPTION_Source));
		s.setSourceVersion("1.5");
		assertEquals(CompilerOptions.VERSION_1_5, s.toNativeSettings().get(CompilerOptions.OPTION_Source));
		s.setSourceVersion("1.6");
		assertEquals(CompilerOptions.VERSION_1_6, s.toNativeSettings().get(CompilerOptions.OPTION_Source));
        s.setSourceVersion("1.7");
        assertEquals(CompilerOptions.VERSION_1_7, s.toNativeSettings().get(CompilerOptions.OPTION_Source));
	}

	public void testTargetVersion() {
		final EclipseJavaCompilerSettings s = new EclipseJavaCompilerSettings();
		s.setTargetVersion("1.1");
		assertEquals(CompilerOptions.VERSION_1_1, s.toNativeSettings().get(CompilerOptions.OPTION_TargetPlatform));
		s.setTargetVersion("1.2");
		assertEquals(CompilerOptions.VERSION_1_2, s.toNativeSettings().get(CompilerOptions.OPTION_TargetPlatform));
		s.setTargetVersion("1.3");
		assertEquals(CompilerOptions.VERSION_1_3, s.toNativeSettings().get(CompilerOptions.OPTION_TargetPlatform));
		s.setTargetVersion("1.4");
		assertEquals(CompilerOptions.VERSION_1_4, s.toNativeSettings().get(CompilerOptions.OPTION_TargetPlatform));
		s.setTargetVersion("1.5");
		assertEquals(CompilerOptions.VERSION_1_5, s.toNativeSettings().get(CompilerOptions.OPTION_TargetPlatform));
		s.setTargetVersion("1.6");
		assertEquals(CompilerOptions.VERSION_1_6, s.toNativeSettings().get(CompilerOptions.OPTION_TargetPlatform));
        s.setTargetVersion("1.7");
        assertEquals(CompilerOptions.VERSION_1_7, s.toNativeSettings().get(CompilerOptions.OPTION_TargetPlatform));
	}

	public void testEncoding() {
		final EclipseJavaCompilerSettings s = new EclipseJavaCompilerSettings();
		s.setSourceEncoding("ASCII");
		assertEquals("ASCII", s.toNativeSettings().get(CompilerOptions.OPTION_Encoding));
	}

	public void testWarnings() {
		final EclipseJavaCompilerSettings s = new EclipseJavaCompilerSettings();
		s.setWarnings(true);
		assertEquals(CompilerOptions.GENERATE, s.toNativeSettings().get(CompilerOptions.OPTION_SuppressWarnings));
		s.setWarnings(false);
		assertEquals(CompilerOptions.DO_NOT_GENERATE, s.toNativeSettings().get(CompilerOptions.OPTION_SuppressWarnings));
	}

	public void testDeprecations() {
		final EclipseJavaCompilerSettings s = new EclipseJavaCompilerSettings();
		s.setDeprecations(true);
		assertEquals(CompilerOptions.GENERATE, s.toNativeSettings().get(CompilerOptions.OPTION_ReportDeprecation));
		s.setDeprecations(false);
		assertEquals(CompilerOptions.DO_NOT_GENERATE, s.toNativeSettings().get(CompilerOptions.OPTION_ReportDeprecation));
	}
}
