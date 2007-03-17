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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jci.ReloadingClassLoader;
import org.apache.commons.jci.listeners.CompilingListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;


/**
 * @author tcurdt
 */
public final class ServerPageServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final ReloadingClassLoader classloader = new ReloadingClassLoader(ServerPageServlet.class.getClassLoader());
	private CompilingListener listener;
	private FilesystemAlterationMonitor fam;

	private Map serverPagesByClassName = new HashMap();

	public void init() throws ServletException {
		super.init();
		
		final File directory = new File(getServletContext().getRealPath("/") + getInitParameter("directory"));
		
		log("monitoring classes in " + directory);

        listener = new CompilingListener() {

			public void onStop( final FilesystemAlterationObserver pObserver ) {
				super.onStop(pObserver);

				final File root = pObserver.getRootDirectory();
								
				final Collection changedFiles = getChangedFiles();
				changedFiles.addAll(getCreatedFiles());
				final Collection deletedFiles = getDeletedFiles();
				
				boolean reload = false;
				
				final Map newServerPagesByClassName = new HashMap(serverPagesByClassName);
				
				for (Iterator it = deletedFiles.iterator(); it.hasNext();) {
					final File file = (File) it.next();
					final String serverPageClassName = convertFileToServerPageClassName(root, file);
					newServerPagesByClassName.remove(serverPageClassName);
					reload = true;
					log("removing " + serverPageClassName);
				}

				for (Iterator it = changedFiles.iterator(); it.hasNext();) {
					final File file = (File) it.next();
					
					final String serverPageClassName = convertFileToServerPageClassName(root, file);
					
					if (serverPageClassName == null) {
						continue;
					}
					
					try {
						final Class clazz = classloader.loadClass(serverPageClassName);
						final HttpServlet serverPage = (HttpServlet) clazz.newInstance();
						newServerPagesByClassName.put(serverPageClassName, serverPage);
						reload = true;
						log("compiled " + serverPageClassName);
					} catch (ClassNotFoundException e) {
						log("", e);
					} catch (InstantiationException e) {
						log("", e);
					} catch (IllegalAccessException e) {
						log("", e);
					}
				}

				if (reload) {
					log("activating new map of serverpages "+ newServerPagesByClassName);
					serverPagesByClassName = newServerPagesByClassName;					
				}

			}        	
        };
        listener.addReloadNotificationListener(classloader);
        
        fam = new FilesystemAlterationMonitor();
        fam.addListener(directory, listener);
        fam.start();
	}

	private String convertFileToServerPageClassName( final File root, final File file ) {

		if (!file.getName().endsWith(".java")) {
			return null;
		}
		
		final String relativeName = file.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);

		log("relative: " + relativeName);
		
		final String clazzName = relativeName.replace('/', '.').substring(0, relativeName.length() - 5); 

		log("clazz: " + clazzName);

		return clazzName;
	}
	
	private String convertRequestToServerPageClassName( final HttpServletRequest request ) {

		final String path = request.getPathInfo().substring(1);

//		log("1 " + request.getContextPath());
//		log("2 " + request.getPathInfo());
//		log("3 " + request.getPathTranslated());
//		log("4 " + request.getRequestURI());
//		log("5 " + request.getServletPath());
//		log("6 " + request.getRequestURL());
		
		// FIXME
		// /some/page/bla.jsp -> some.page.Bla
		
		final String clazz = path;
		
		return clazz;
	}
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log("request " + request.getRequestURI());
		
		final String serverPageNameClassName = convertRequestToServerPageClassName(request);

		log("checking for serverpage " + serverPageNameClassName);
		
		final HttpServlet serverPage = (HttpServlet) serverPagesByClassName.get(serverPageNameClassName);
		
		if (serverPage == null) {
			log("no serverpage  for " + request.getRequestURI());
			response.sendError(404);
			return;
		}

		log("delegating request to " + serverPageNameClassName);
		
		serverPage.service(request, response);
	}

	public void destroy() {
		
		fam.stop();
		
		super.destroy();		
	}
}
