package org.apache.commons.jci;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.jci.classes.ExtendedDump;
import org.apache.commons.jci.classes.SimpleDump;
import org.apache.commons.jci.compilers.CompilationResult;
import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.listeners.CompilingListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.jci.problems.CompilationProblem;
import org.apache.commons.jci.problems.CompilationProblemHandler;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.jci.utils.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class CompilingClassLoaderTestCase extends AbstractTestCase {

	private final Log log = LogFactory.getLog(CompilingClassLoaderTestCase.class);

    private ReloadingClassLoader classloader;
    private CompilingListener listener;
    private FilesystemAlterationMonitor fam;
    
//    private final static class BeanUtils {
//        
//        public static void setProperty( Object object, String property, Object value) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
//            final Class clazz = object.getClass();
//            
//            final Method setter = clazz.getMethod("set" + property, new Class[]{ value.getClass()});
//            setter.invoke(object, new Object[]{ value });   
//        }
//    }
    
    
    private final static class MockJavaCompiler implements JavaCompiler {

        private final Log log = LogFactory.getLog(MockJavaCompiler.class);

		public CompilationResult compile(String[] pResourcePaths, ResourceReader pReader, ResourceStore pStore, ClassLoader classLoader) {
			
			for (int i = 0; i < pResourcePaths.length; i++) {
				final String resourcePath = pResourcePaths[i];				
				final byte[] resourceContent = pReader.getBytes(resourcePath);
				
				log.debug("resource " + resourcePath + " = " + ((resourceContent!=null)?new String(resourceContent):null) );
				
				final byte[] data;
				
				if ("jci/Simple.java".equals(resourcePath)) {

					try {
						data = SimpleDump.dump(new String(resourceContent));
					} catch (Exception e) {
						throw new RuntimeException("cannot handle resource " + resourcePath, e);
					}
					
				} else if ("jci/Extended.java".equals(resourcePath)) {

					try {
						data = ExtendedDump.dump();
					} catch (Exception e) {
						throw new RuntimeException("cannot handle resource " + resourcePath, e);
					}
					
				} else {
					throw new RuntimeException("cannot handle resource " + resourcePath);
				}

				log.debug("compiling " + resourcePath + " (" + data.length + ")");
				
				pStore.write(ClassUtils.stripExtension(resourcePath) + ".class", data);

			}
			
			return new CompilationResult(new CompilationProblem[0]);
		}

		public CompilationResult compile(String[] pResourcePaths, ResourceReader pReader, ResourceStore pStore) {
			return compile(pResourcePaths, pReader, pStore, null);
		}

		public void setCompilationProblemHandler(CompilationProblemHandler pHandler) {
		}
    	
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        classloader = new ReloadingClassLoader(this.getClass().getClassLoader());
        listener = new CompilingListener(new MockJavaCompiler());   

        listener.addReloadNotificationListener(classloader);
        
        fam = new FilesystemAlterationMonitor();
        fam.addListener(directory, listener);
        fam.start();
    }

    private void initialCompile() throws Exception {
        log.debug("initial compile");        

        listener.waitForFirstCheck();
                
        writeFile("jci/Simple.java", "Simple1");        
        writeFile("jci/Extended.java", "Extended");        
        
        log.debug("waiting for compile changes to get applied");        
        listener.waitForCheck();
        
        log.debug("*** ready to test");        
    }
    
    
//    public void testCompileProblems() throws Exception {
//        delay();        
//        writeFile("jci/Simple.java", "JavaSources.error");
//        listener.waitForEvent();
//        
//        // FIXME
//    }
    
    public void testCreate() throws Exception {
        initialCompile();
        
        log.debug("loading Simple");        
        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple1", simple.toString());
        
        log.debug("loading Extended");        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple1", extended.toString());
    }

    public void testChange() throws Exception {        
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple1", simple.toString());
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple1", extended.toString());

        delay();
        writeFile("jci/Simple.java", "Simple2");
        listener.waitForCheck();
    
        final Object simple2 = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple2", simple2.toString());
        
        final Object newExtended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple2", newExtended.toString());
    }

    public void testDelete() throws Exception {
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple1", simple.toString());
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple1", extended.toString());
                
        listener.waitForCheck();
        
        log.debug("deleting source file");
        assertTrue(new File(directory, "jci/Extended.java").delete());
        
        listener.waitForCheck();
       
        log.debug("loading Simple");
        final Object oldSimple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple1", oldSimple.toString());

        log.debug("trying to loading Extended");
        try {
            classloader.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertEquals("jci.Extended", e.getMessage());
        }        
        
        log.debug("deleting whole directory");
        FileUtils.deleteDirectory(new File(directory, "jci"));

        listener.waitForCheck();

        log.debug("trying to loading Simple");
        try {
            classloader.loadClass("jci.Simple").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertEquals("jci.Simple", e.getMessage());
        }

    }

    public void testDeleteDependency() throws Exception {        
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertEquals("Simple1", simple.toString());
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertEquals("Extended:Simple1", extended.toString());
        
        log.debug("deleting source file");
        assertTrue(new File(directory, "jci/Simple.java").delete());
        listener.waitForCheck();

        log.debug("trying to load dependend class");
        try {
            classloader.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final NoClassDefFoundError e) {
            assertEquals("jci/Simple", e.getMessage());
        }
        
    }


//    public void testReference1() throws Exception {        
//        delay();        
//        writeFile("jci/Foo.java",
//                "package jci;\n" + 
//                "\n" + 
//                "public class Foo {\n" + 
//                "    public String toString() {\n" + 
//                "        return \"foo1\";\n" + 
//                "    }\n" + 
//                "}"
//                );        
//        writeFile("jci/Bar.java",
//                "package jci;\n" + 
//                "\n" + 
//                "public class Bar {\n" + 
//                "    \n" + 
//                "    private Foo foo;\n" + 
//                "    \n" + 
//                "    public void setFoo( Foo foo) {\n" + 
//                "        this.foo = foo;\n" + 
//                "    }\n" + 
//                "    \n" + 
//                "    public String toString() {\n" + 
//                "        return \"bar1\";\n" + 
//                "    }\n" + 
//                "}"
//                );        
//        listener.waitForEvent();
//        
//        final Object foo1 = classloader.loadClass("jci.Foo").newInstance();        
//        assertTrue("foo1".equals(foo1.toString()));
//
//        final Object bar1 = classloader.loadClass("jci.Bar").newInstance();        
//        assertTrue("bar1".equals(bar1.toString()));
//        
//        BeanUtils.setProperty(bar1, "Foo", foo1);
//        
//        delay();
//        writeFile("jci/Foo.java",
//                "package jci;\n" + 
//                "\n" + 
//                "public class Foo {\n" + 
//                "    public String toString() {\n" + 
//                "        return \"foo2\";\n" + 
//                "    }\n" + 
//                "}"
//                );        
//        listener.waitForEvent();
//
//        final Object foo2 = classloader.loadClass("jci.Foo").newInstance();        
//        assertTrue("foo2".equals(foo2.toString()));
//
//        final Object bar2 = classloader.loadClass("jci.Bar").newInstance();        
//        // has not change -> still bar1
//        assertTrue("bar1".equals(bar2.toString()));
//    
//        BeanUtils.setProperty(bar2, "Foo", foo2);
//        BeanUtils.setProperty(bar1, "Foo", foo2);
//
//    }
//
//    public void testReference2() throws Exception {        
//        delay();        
//        writeFile("jci/Foo.java",
//                "package jci;\n" + 
//                "\n" + 
//                "public class Foo implements org.apache.commons.jci.MyFoo {\n" + 
//                "    public String toString() {\n" + 
//                "        return \"foo1\";\n" + 
//                "    }\n" + 
//                "}"
//                );        
//        listener.waitForEvent();
//        
//        final MyFoo foo1 = (MyFoo) classloader.loadClass("jci.Foo").newInstance();        
//        assertTrue("foo1".equals(foo1.toString()));
//
//
//        final MyBar bar1 = new MyBar();
//        bar1.setFoo(foo1);
//        
//        delay();
//        writeFile("jci/Foo.java",
//                "package jci;\n" + 
//                "\n" + 
//                "public class Foo implements org.apache.commons.jci.MyFoo {\n" + 
//                "    public String toString() {\n" + 
//                "        return \"foo2\";\n" + 
//                "    }\n" + 
//                "}"
//                );        
//        listener.waitForEvent();
//
//        final MyFoo foo2 = (MyFoo) classloader.loadClass("jci.Foo").newInstance();        
//        assertTrue("foo2".equals(foo2.toString()));
//
//        bar1.setFoo(foo2);
//    }




    protected void tearDown() throws Exception {
        fam.removeListener(listener);
        fam.stop();
        super.tearDown();
    }
    
}
