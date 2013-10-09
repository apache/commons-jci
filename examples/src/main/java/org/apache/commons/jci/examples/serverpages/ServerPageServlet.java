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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.String;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jci.ReloadingClassLoader;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.listeners.CompilingListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;
import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.TransactionalResourceStore;
import org.apache.commons.jci.utils.ConversionUtils;


/**
 * A mini JSP servlet that monitors a certain directory and
 * recompiles and then instantiates the JSP pages as soon as
 * they have changed.
 *
 * @author tcurdt
 */
public final class ServerPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ReloadingClassLoader classloader = new ReloadingClassLoader(ServerPageServlet.class.getClassLoader());
    private FilesystemAlterationMonitor fam;
    private CompilingListener jspListener; 

    private Map<String, HttpServlet> servletsByClassname = new HashMap<String, HttpServlet>();

    public void init() throws ServletException {
        super.init();

        final File serverpagesDir = new File(getServletContext().getRealPath("/") + getInitParameter("serverpagesDir"));

        log("Monitoring serverpages in " + serverpagesDir);

        final TransactionalResourceStore store = new TransactionalResourceStore(new MemoryResourceStore()) {

            private Set<String> newClasses;
            private Map<String, HttpServlet> newServletsByClassname;

            public void onStart() {
                super.onStart();

                newClasses = new HashSet<String>();
                newServletsByClassname = new HashMap<String, HttpServlet>(servletsByClassname);
            }

            public void onStop() {
                super.onStop();

                boolean reload = false;
                for (String clazzName : newClasses) {
                    try {
                        final Class clazz = classloader.loadClass(clazzName);

                        if (!HttpServlet.class.isAssignableFrom(clazz)) {
                            log(clazzName + " is not a servlet");
                            continue;
                        }

                        // create new instance of jsp page
                        final HttpServlet servlet = (HttpServlet) clazz.newInstance();
                        newServletsByClassname.put(clazzName, servlet);

                        reload = true;
                    } catch(Exception e) {
                        log("", e);
                    }
                }

                if (reload) {
                    log("Activating new map of servlets "+ newServletsByClassname);
                    servletsByClassname = newServletsByClassname;
                }
            }

            public void write(String pResourceName, byte[] pResourceData) {
                super.write(pResourceName, pResourceData);

                if (pResourceName.endsWith(".class")) {

                    // compiler writes a new class, remember the classes to reload
                    newClasses.add(pResourceName.replace('/', '.').substring(0, pResourceName.length() - ".class".length()));
                }
            }

        };

        // listener that generates the java code from the jsp page and provides that to the compiler
        jspListener = new CompilingListener(new JavaCompilerFactory().createCompiler("eclipse"), store) {

            private final JspGenerator transformer = new JspGenerator();
            private final Map<String, byte[]> sources = new HashMap<String, byte[]>();
            private final Set<String> resourceToCompile = new HashSet<String>();

            public void onStart(FilesystemAlterationObserver pObserver) {
                super.onStart(pObserver);

                resourceToCompile.clear();
            }


            public void onFileChange(File pFile) {
                if (pFile.getName().endsWith(".jsp")) {
                    final String resourceName = ConversionUtils.stripExtension(getSourceNameFromFile(observer, pFile)) + ".java";

                    log("Updating " + resourceName);

                    sources.put(resourceName, transformer.generateJavaSource(resourceName, pFile));

                    resourceToCompile.add(resourceName);
                }
                super.onFileChange(pFile);
            }


            public void onFileCreate(File pFile) {
                if (pFile.getName().endsWith(".jsp")) {
                    final String resourceName = ConversionUtils.stripExtension(getSourceNameFromFile(observer, pFile)) + ".java";

                    log("Creating " + resourceName);

                    sources.put(resourceName, transformer.generateJavaSource(resourceName, pFile));

                    resourceToCompile.add(resourceName);
                }
                super.onFileCreate(pFile);
            }


            public String[] getResourcesToCompile(FilesystemAlterationObserver pObserver) {
                // we only want to compile the jsp pages
                final String[] resourceNames = new String[resourceToCompile.size()];
                resourceToCompile.toArray(resourceNames);
                return resourceNames;
            }


            public ResourceReader getReader( final FilesystemAlterationObserver pObserver ) {
                return new JspReader(sources, super.getReader(pObserver));
            }
        };
        jspListener.addReloadNotificationListener(classloader);
        
        fam = new FilesystemAlterationMonitor();
        fam.addListener(serverpagesDir, jspListener);
        fam.start();
    }

    private String convertRequestToServletClassname( final HttpServletRequest request ) {

        String path = request.getPathInfo().substring(1);

        return ConversionUtils.stripExtension(path).replace('/', '.');
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log("Request " + request.getRequestURI());

        final CompilationResult result = jspListener.getCompilationResult();
        final CompilationProblem[] errors = result.getErrors();

        if (errors.length > 0) {

            // if there are errors we provide the compilation errors instead of the jsp page

            final PrintWriter out = response.getWriter();

            out.append("<html><body>");

            for (CompilationProblem problem : errors) {
                out.append(problem.toString()).append("<br/>").append('\n');
            }

            out.append("</body></html>");

            out.flush();
            out.close();
            return;
        }

        final String servletClassname = convertRequestToServletClassname(request);

        log("Checking for serverpage " + servletClassname);

        final HttpServlet servlet = servletsByClassname.get(servletClassname);

        if (servlet == null) {
            log("No servlet  for " + request.getRequestURI());
            response.sendError(404);
            return;
        }

        log("Delegating request to " + servletClassname);

        servlet.service(request, response);
    }

    public void destroy() {

        fam.stop();

        super.destroy();
    }
}
