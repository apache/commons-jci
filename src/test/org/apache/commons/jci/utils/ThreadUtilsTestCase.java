package org.apache.commons.jci.utils;

import junit.framework.TestCase;


public final class ThreadUtilsTestCase extends TestCase {

    public void testSleep() {
        final long start = System.currentTimeMillis();
        ThreadUtils.sleep(1000);
        final long stop = System.currentTimeMillis();
        assertTrue( Math.abs( (stop-start) - 1000) < 200 );
    }

    public void testSleepInterruption() throws InterruptedException {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                ThreadUtils.sleep(2000);
            }
        });
        thread.start();
        final long start = System.currentTimeMillis();
        thread.interrupt();
        thread.join();
        final long stop = System.currentTimeMillis();
        assertTrue( (stop-start) < 1000 );
    }
}
