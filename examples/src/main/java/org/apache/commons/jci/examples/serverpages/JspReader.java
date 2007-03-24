package org.apache.commons.jci.examples.serverpages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.utils.ConversionUtils;

public final class JspReader implements ResourceReader {

	private final ResourceReader reader;
	
	
	public JspReader( final ResourceReader pReader ) {
		reader = pReader;
	}
	
	private String quote( final String s ) {
		
		final StringBuffer sb = new StringBuffer();
		final char[] input = s.toCharArray();
		
		for (int i = 0; i < input.length; i++) {
			final char c = input[i];
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
	
	private void generateJavaCodeFor( final StringBuffer pInput, final Writer pOutput ) throws IOException {
		
		pOutput.append("    out.write(\"");
		
		pOutput.append(quote(pInput.toString()));

		pOutput.append("\");").append('\n');
	}
	
	private byte[] transform( String pResourceName, byte[] pBytes ) {

		try {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			final Reader input = new InputStreamReader(new ByteArrayInputStream(pBytes));
			final Writer output = new OutputStreamWriter(outputStream);
			
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

			StringBuffer sb = new StringBuffer();
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
							
							generateJavaCodeFor(sb, output);

							sb = new StringBuffer();
							watch = close;
						} else if (watch == close) {
							// found close

							// <? ... ?> is java
							output.append(sb.toString());
							
							sb = new StringBuffer();
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
				generateJavaCodeFor(sb, output);
			}

			
			output.append("    out.close();").append('\n');
			output.append("    out.flush();").append('\n');
			output.append("  }").append('\n');
			output.append("}").append('\n');
			
			
			output.close();
			
			return outputStream.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}
	
	public byte[] getBytes( String pResourceName ) {
		final byte[] resourceBytes = reader.getBytes(pResourceName);
		
		if (resourceBytes == null) {
			return null;
		}
		
		final byte[] jspServletCode = transform(pResourceName, resourceBytes);
		
		System.out.println(new String(jspServletCode));
		
		return jspServletCode;
	}

	public boolean isAvailable( String pResourceName ) {
		return reader.isAvailable(pResourceName);
	}

}
