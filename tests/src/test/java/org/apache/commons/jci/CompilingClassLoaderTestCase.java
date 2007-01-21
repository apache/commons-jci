package org.apache.commons.jci;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.apache.commons.jci.compilers.AbstractCompilerTestCase;
import org.apache.commons.jci.compilers.JavaSources;
import org.apache.commons.jci.listeners.CompilingListener;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;


public final class CompilingClassLoaderTestCase extends AbstractCompilerTestCase {

    private ReloadingClassLoader classloader;
    private CompilingListener listener;
    private FilesystemAlterationMonitor fam;
    
    private final static class BeanUtils {
        
        public static void setProperty( Object object, String property, Object value) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            final Class clazz = object.getClass();
            
            final Method setter = clazz.getMethod("set" + property, new Class[]{ value.getClass()});
            setter.invoke(object, new Object[]{ value });   
        }
    }
    
    
    protected void setUp() throws Exception {
        super.setUp();
        
        classloader = new ReloadingClassLoader(this.getClass().getClassLoader());
        listener = new CompilingListener(directory);   
        classloader.addListener(listener);
        
        fam = new FilesystemAlterationMonitor();
        fam.addListener(listener);
        fam.start();
    }

    private void initialCompile() throws Exception {
        delay();        
        writeFile("jci/Simple.java", JavaSources.simple);        
        writeFile("jci/Extended.java", JavaSources.extended);        
        listener.waitForEvent();
    }
    
    
    public void testCompileProblems() throws Exception {
        delay();        
        writeFile("jci/Simple.java", JavaSources.error);
        listener.waitForEvent();
        
        // FIXME
    }
    
    public void testCreate() throws Exception {
        initialCompile();
        
        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));
    }

    public void testChange() throws Exception {        
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));

        delay();
        writeFile("jci/Simple.java", JavaSources.SIMPLE);
        listener.waitForEvent();
    
        final Object SIMPLE = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("SIMPLE".equals(SIMPLE.toString()));
        
        final Object newExtended = classloader.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:SIMPLE".equals(newExtended.toString()));
    }

    public void testDelete() throws Exception {
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));
        
        delay();
        assertTrue(new File(directory, "jci/Extended.java").delete());
        listener.waitForEvent();

        final Object oldSimple = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(oldSimple.toString()));

        try {
            classloader.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertTrue("jci.Extended".equals(e.getMessage()));
        }
        
        delay();
        FileUtils.deleteDirectory(new File(directory, "jci"));
        listener.waitForEvent();

        try {
            classloader.loadClass("jci.Simple").newInstance();
            fail();
        } catch(final ClassNotFoundException e) {
            assertTrue("jci.Simple".equals(e.getMessage()));
        }

    }

    public void testDeleteDependency() throws Exception {        
        initialCompile();

        final Object simple = classloader.loadClass("jci.Simple").newInstance();        
        assertTrue("Simple".equals(simple.toString()));
        
        final Object extended = classloader.loadClass("jci.Extended").newInstance();        
        assertTrue("Extended:Simple".equals(extended.toString()));
        
        delay();
        assertTrue(new File(directory, "jci/Simple.java").delete());
        listener.waitForEvent();

        try {
            classloader.loadClass("jci.Extended").newInstance();
            fail();
        } catch(final NoClassDefFoundError e) {
            assertTrue("jci/Simple".equals(e.getMessage()));
        }
        
    }


    public void testReference1() throws Exception {        
        delay();        
        writeFile("jci/Foo.java",
                "package jci;\n" + 
                "\n" + 
                "public class Foo {\n" + 
                "    public String toString() {\n" + 
                "        return \"foo1\";\n" + 
                "    }\n" + 
                "}"
                );        
        writeFile("jci/Bar.java",
                "package jci;\n" + 
                "\n" + 
                "public class Bar {\n" + 
                "    \n" + 
                "    private Foo foo;\n" + 
                "    \n" + 
                "    public void setFoo( Foo foo) {\n" + 
                "        this.foo = foo;\n" + 
                "    }\n" + 
                "    \n" + 
                "    public String toString() {\n" + 
                "        return \"bar1\";\n" + 
                "    }\n" + 
                "}"
                );        
        listener.waitForEvent();
        
        final Object foo1 = classloader.loadClass("jci.Foo").newInstance();        
        assertTrue("foo1".equals(foo1.toString()));

        final Object bar1 = classloader.loadClass("jci.Bar").newInstance();        
        assertTrue("bar1".equals(bar1.toString()));
        
        BeanUtils.setProperty(bar1, "Foo", foo1);
        
        delay();
        writeFile("jci/Foo.java",
                "package jci;\n" + 
                "\n" + 
                "public class Foo {\n" + 
                "    public String toString() {\n" + 
                "        return \"foo2\";\n" + 
                "    }\n" + 
                "}"
                );        
        listener.waitForEvent();

        final Object foo2 = classloader.loadClass("jci.Foo").newInstance();        
        assertTrue("foo2".equals(foo2.toString()));

        final Object bar2 = classloader.loadClass("jci.Bar").newInstance();        
        // has not change -> still bar1
        assertTrue("bar1".equals(bar2.toString()));
    
        BeanUtils.setProperty(bar2, "Foo", foo2);
        BeanUtils.setProperty(bar1, "Foo", foo2);

    }

    public void testReference2() throws Exception {        
        delay();        
        writeFile("jci/Foo.java",
                "package jci;\n" + 
                "\n" + 
                "public class Foo implements org.apache.commons.jci.MyFoo {\n" + 
                "    public String toString() {\n" + 
                "        return \"foo1\";\n" + 
                "    }\n" + 
                "}"
                );        
        listener.waitForEvent();
        
        final MyFoo foo1 = (MyFoo) classloader.loadClass("jci.Foo").newInstance();        
        assertTrue("foo1".equals(foo1.toString()));


        final MyBar bar1 = new MyBar();
        bar1.setFoo(foo1);
        
        delay();
        writeFile("jci/Foo.java",
                "package jci;\n" + 
                "\n" + 
                "public class Foo implements org.apache.commons.jci.MyFoo {\n" + 
                "    public String toString() {\n" + 
                "        return \"foo2\";\n" + 
                "    }\n" + 
                "}"
                );        
        listener.waitForEvent();

        final MyFoo foo2 = (MyFoo) classloader.loadClass("jci.Foo").newInstance();        
        assertTrue("foo2".equals(foo2.toString()));

        bar1.setFoo(foo2);
    }




    protected void tearDown() throws Exception {
        fam.removeListener(listener);
        fam.stop();
        super.tearDown();
    }
    
}
