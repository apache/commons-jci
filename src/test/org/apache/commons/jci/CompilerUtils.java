package org.apache.commons.jci;

import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.eclipse.EclipseJavaCompiler;
import org.apache.commons.jci.problems.LogCompilationProblemHandler;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class CompilerUtils {
    
    private final static Log log = LogFactory.getLog(CompilerUtils.class);
    
    private CompilerUtils() {       
    }
    
    public static byte[] compile( final String[] pClazzes, final String[] pPrograms ) {
        final JavaCompiler compiler = new EclipseJavaCompiler();
        final ResourceStore store = new MemoryResourceStore();        
        compiler.compile(
                pClazzes,
                new ResourceReader() {
                    public char[] getContent( String pFileName ) {
                        for (int i = 0; i < pPrograms.length; i++) {
                            final String clazzName = pClazzes[i].replace('.', '/') + ".java";
                            if (clazzName.equals(pFileName)) {
                                return pPrograms[i].toCharArray();
                            }
                        }
                        return null;
                    }
                    public boolean isAvailable( String pFileName ) {
//                        for (int i = 0; i < pPrograms.length; i++) {
//                            final String clazzName = pClazzes[i].replace('.', '/') + ".class";
//                            if (clazzName.equals(pFileName)) {
//                                return true;
//                            }
//                        }
                        return false;
                    }
                },
                store,
                new LogCompilationProblemHandler()
                );
        return store.read(pClazzes[0]);
    }
    
    public static byte[] compile( final String pClazz, final String pProgramm ) {
        final JavaCompiler compiler = new EclipseJavaCompiler();
        final ResourceStore store = new MemoryResourceStore();        
        compiler.compile(
                new String[] { pClazz },
                new ResourceReader() {
                    public char[] getContent( String pFileName ) {
                        return pProgramm.toCharArray();
                    }
                    public boolean isAvailable( String pFilename ) {
                        return false;
                    }
                },
                store,
                new LogCompilationProblemHandler()
                );
        return store.read(pClazz);
    }
    
//    public static void compileTo( final String pClazz, final String pProgramm, final File pDestination ) throws IOException {
//        final File parent = pDestination.getParentFile();
//        if (!parent.exists()) {
//            if (!parent.mkdirs()) {
//                throw new IOException("could not create" + parent);
//            }
//        }
//        final FileOutputStream os = new FileOutputStream(pDestination);
//        final byte[] result = compile(pClazz, pProgramm);
//        log.debug("writing " + result.length + " bytes to " + pDestination.getAbsolutePath());
//        os.write(result);
//        os.flush();
//        os.close();
//    }
}
