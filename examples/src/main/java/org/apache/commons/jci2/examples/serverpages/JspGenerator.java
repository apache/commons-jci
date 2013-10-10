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

package org.apache.commons.jci2.examples.serverpages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.jci2.utils.ConversionUtils;

/**
 * @author tcurdt
 */
public final class JspGenerator {

    private String quote( final String s ) {

        final StringBuilder sb = new StringBuilder();
        final char[] input = s.toCharArray();

        for (char c : input) {
            if (c == '"') {
                sb.append('\\');
            }
            if (c == '\\') {
                sb.append('\\');
            }

            if (c == '\n') {
                sb.append("\");\n").append("    out.write(\"");
                continue;
            }
            sb.append(c);
        }

        return sb.toString();
    }

    private void wrap( final StringBuilder pInput, final Writer pOutput ) throws IOException {

        pOutput.append("    out.write(\"");

        pOutput.append(quote(pInput.toString()));

        pOutput.append("\");").append('\n');
    }

    public byte[] generateJavaSource( final  String pResourceName, final File pFile ) {

    	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Writer output = new OutputStreamWriter(outputStream);

        try {
            final Reader input = new InputStreamReader(new FileInputStream(pFile));

            final int p = pResourceName.lastIndexOf('/');

            final String className;
            final String packageName;

            if (p < 0) {
                className = ConversionUtils.stripExtension(pResourceName);
                packageName = "";
            } else {
                className = ConversionUtils.stripExtension(pResourceName.substring(p+1));
                packageName = pResourceName.substring(0, p).replace('/', '.');
                output.append("package ").append(packageName).append(";").append('\n');
            }


            output.append("import java.io.PrintWriter;").append('\n');
            output.append("import java.io.IOException;").append('\n');
            output.append("import javax.servlet.http.HttpServlet;").append('\n');
            output.append("import javax.servlet.http.HttpServletRequest;").append('\n');
            output.append("import javax.servlet.http.HttpServletResponse;").append('\n');
            output.append("import javax.servlet.ServletException;").append('\n');
            output.append("public class ").append(className).append(" extends HttpServlet {").append('\n');
            output.append("  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {").append('\n');
            output.append("    final PrintWriter out = response.getWriter();").append('\n');


            final char[] open = "<?".toCharArray();
            final char[] close = "?>".toCharArray();

            StringBuilder sb = new StringBuilder();
            char[] watch = open;
            int w = 0;
            while(true) {
                int c = input.read();

                if (c < 0) {
                    break;
                }

                if (c == watch[w]) {
                    w++;
                    if (watch.length == w) {
                        if (watch == open) {
                            // found open

                            wrap(sb, output);

                            sb = new StringBuilder();
                            watch = close;
                        } else if (watch == close) {
                            // found close

                            // <? ... ?> is java
                            output.append(sb.toString());

                            sb = new StringBuilder();
                            watch = open;
                        }
                        w = 0;
                    }
                } else {
                    if (w > 0) {
                        sb.append(watch, 0, w);
                    }

                    sb.append((char)c);

                    w = 0;
                }
            }

            if (watch == open) {
                wrap(sb, output);
            }


            output.append("    out.close();").append('\n');
            output.append("    out.flush();").append('\n');
            output.append("  }").append('\n');
            output.append("}").append('\n');

            return outputStream.toByteArray();

        } catch (IOException e) {
            return null;
        } finally {
            try {
				output.close();
			} catch (IOException e) {
			}        	
        }
    }

}
