package org.apache.commons.jci.monitor;

import java.io.File;


public final class CommandlineTest {

    public static void main( String[] args ) {
        
        File repository = new File("/Users/tcurdt/dev/cocoon-trunk/eclipse");
        //File repository = new File("/Users/tcurdt/dev/test");
               
        FilesystemAlterationMonitor fam = new FilesystemAlterationMonitor(); 

        fam.addListener(new FilesystemAlterationListener() {
            public void onStart() {
                System.out.println("start");
            }
            public void onStop() {
                System.out.println("stop");
            }

            public void onCreateFile( final File file ) {
                System.out.println("create file");
            }
            public void onChangeFile( final File file ) {                
                System.out.println("change file");
            }
            public void onDeleteFile( final File file ) {
                System.out.println("delete file");
            }

            public void onCreateDirectory( final File file ) {                
                System.out.println("create dir");
            }
            public void onChangeDirectory( final File file ) {                
                System.out.println("change dir");
            }
            public void onDeleteDirectory( final File file ) {
                System.out.println("delete dir");
            }
            }, repository);
        
        Thread myThread = new Thread(fam); 
        myThread.start();
        
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
