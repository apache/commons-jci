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

import junit.framework.TestCase;

public final class JavacJavaCompilerSettingsTestCase extends TestCase {

	private static void assertEquals(String[] expected, String[] result) {
		assertEquals(expected.length, result.length);

		for (int i = 0; i < expected.length; i++) {
			assertEquals("Unexpected value at index " + i, expected[i], result[i]);
		}
	}
	
	public void testDefaultSettings() {
		final String[] expected = {
			"-nowarn",
			"-target", "1.4",
			"-source", "1.4",
			"-encoding", "UTF-8"				
		};
		
		final String[] s = new JavacJavaCompilerSettings().toNativeSettings();

		assertEquals(expected, s);
	}

	public void testSourceVersion() {
		final JavacJavaCompilerSettings s = new JavacJavaCompilerSettings();
		s.setSourceVersion("1.1");
		assertEquals("1.1", s.toNativeSettings()[4]);
		s.setSourceVersion("1.2");
		assertEquals("1.2", s.toNativeSettings()[4]);
		s.setSourceVersion("1.3");
		assertEquals("1.3", s.toNativeSettings()[4]);
		s.setSourceVersion("1.4");
		assertEquals("1.4", s.toNativeSettings()[4]);
		s.setSourceVersion("1.5");
		assertEquals("1.5", s.toNativeSettings()[4]);
		s.setSourceVersion("1.6");
		assertEquals("1.6", s.toNativeSettings()[4]);
        s.setSourceVersion("1.7");
        assertEquals("1.7", s.toNativeSettings()[4]);
	}

	public void testTargetVersion() {
		final JavacJavaCompilerSettings s = new JavacJavaCompilerSettings();
		s.setTargetVersion("1.1");
		assertEquals("1.1", s.toNativeSettings()[2]);
		s.setTargetVersion("1.2");
		assertEquals("1.2", s.toNativeSettings()[2]);
		s.setTargetVersion("1.3");
		assertEquals("1.3", s.toNativeSettings()[2]);
		s.setTargetVersion("1.4");
		assertEquals("1.4", s.toNativeSettings()[2]);
		s.setTargetVersion("1.5");
		assertEquals("1.5", s.toNativeSettings()[2]);
		s.setTargetVersion("1.6");
		assertEquals("1.6", s.toNativeSettings()[2]);
        s.setTargetVersion("1.7");
        assertEquals("1.7", s.toNativeSettings()[2]);
	}

	public void testEncoding() {
		final JavacJavaCompilerSettings s = new JavacJavaCompilerSettings();
		s.setSourceEncoding("ASCII");
		assertEquals("ASCII", s.toNativeSettings()[6]);
	}

	public void testWarnings() {
		final JavacJavaCompilerSettings s = new JavacJavaCompilerSettings();
		s.setWarnings(true);
		assertFalse("-nowarn".equals(s.toNativeSettings()[0]));
		s.setWarnings(false);
		assertEquals("-nowarn", s.toNativeSettings()[0]);

	}

	public void testDeprecations() {
		final JavacJavaCompilerSettings s = new JavacJavaCompilerSettings();
		s.setDeprecations(true);
		assertEquals("-deprecation", s.toNativeSettings()[0]);
		s.setDeprecations(false);
		assertFalse("-deprecation".equals(s.toNativeSettings()[0]));
	}
}
