package org.apache.commons.jci;

import org.apache.commons.jci.compilers.JavaCompiler;
import org.apache.commons.jci.compilers.JavaCompilerFactory;
import org.apache.commons.jci.readers.ResourceReader;
import org.apache.commons.jci.stores.MemoryResourceStore;
import org.apache.commons.jci.stores.ResourceStore;


public final class CompilerUtils {
    
    private CompilerUtils() {       
    }
    
    public static byte[] compile( final String[] pClazzes, final String[] pPrograms ) {
        final JavaCompiler compiler = JavaCompilerFactory.getInstance().createCompiler("eclipse");
        final ResourceStore store = new MemoryResourceStore();        
        compiler.compile(
                pClazzes,
                new ResourceReader() {
                    public byte[] getBytes( String pResourceName ) {
                        for (int i = 0; i < pPrograms.length; i++) {
                            final String clazzName = pClazzes[i].replace('.', '/') + ".java";
                            if (clazzName.equals(pResourceName)) {
                                return pPrograms[i].getBytes();
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
                store
                );
        return store.read(pClazzes[0]);
    }
    
    public static byte[] compile( final String pClazz, final String pProgramm ) {
        final JavaCompiler compiler = JavaCompilerFactory.getInstance().createCompiler("eclipse");
        final ResourceStore store = new MemoryResourceStore();        
        compiler.compile(
                new String[] { pClazz },
                new ResourceReader() {
                    public byte[] getBytes( String pFileName ) {
                        return pProgramm.getBytes();
                    }
                    public boolean isAvailable( String pFilename ) {
                        return false;
                    }
                },
                store
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
