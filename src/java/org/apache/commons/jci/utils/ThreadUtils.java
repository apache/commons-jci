package org.apache.commons.jci.utils;


public final class ThreadUtils {

    public static void sleep( final long pDelay ) {
        try {
            Thread.sleep(pDelay);
        } catch (final InterruptedException e) {
        }
    }
    
}
