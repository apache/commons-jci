/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.jci2.core;

import org.apache.commons.jci2.core.classes.SimpleDump;
import org.apache.commons.jci2.core.stores.MemoryResourceStore;
import org.apache.commons.jci2.core.stores.ResourceStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Tests {@link ReloadingClassLoader#removeResourceStore(ResourceStore)}.
 * method.
 */
public class ReloadingClassLoaderRemoveTestCase extends TestCase {

    private final Log log = LogFactory.getLog(ReloadingClassLoaderRemoveTestCase.class);

    private final byte[] clazzSimpleA;
    private final MemoryResourceStore store1 = new MemoryResourceStore();
    private final MemoryResourceStore store2 = new MemoryResourceStore();
    private final MemoryResourceStore store3 = new MemoryResourceStore();
    private final MemoryResourceStore store4 = new MemoryResourceStore();

    public ReloadingClassLoaderRemoveTestCase() throws Exception {
        clazzSimpleA = SimpleDump.dump("SimpleA");
        assertTrue(clazzSimpleA.length > 0);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /**
     * Test trying to remove a ResourceStore from the ReloadingClassLoader
     * which can't be found - when the ClassLoader contains NO other ResourceStore.
     *
     * Bug: The While loop in the removeResourceStore() throws an ArrayOutOfBoundsException
     */
    public void testRemoveStoreNotFoundClassLoaderNoStores() {
        final ReloadingClassLoader loader = new ReloadingClassLoader(getClass().getClassLoader());
        checkRemoveResourceStore("No ResourceStore", loader, store1, false);
    }

    /**
     * Test trying to remove a ResourceStore from the ReloadingClassLoader
     * which can't be found - when the ClassLoader DOES contain other ResourceStore.
     *
     * Bug: The While loop in the removeResourceStore() throws an ArrayOutOfBoundsException
     */
    public void testRemoveStoreNotFoundClassLoaderHasStores() {
        final ReloadingClassLoader loader = new ReloadingClassLoader(getClass().getClassLoader());
        loader.addResourceStore(store1);
        loader.addResourceStore(store2);
        checkRemoveResourceStore("Has ResourceStore", loader, store3, false);
    }

    /**
     * Test trying to remove the first ResourceStore added
     *
     * Bug: ReloadingClassLoader addes ResourceStore at the start of the array. Removing the
     *      first one added (last in array) causes the second System.arraycopy() statement to throw a
     *      ArrayIndexOutOfBoundsException because the destination array position in the new smaller
     *      array is too large.
     */
    public void testRemoveStoresOne() {
        final ReloadingClassLoader loader = new ReloadingClassLoader(getClass().getClassLoader());
        loader.addResourceStore(store1);
        loader.addResourceStore(store2);
        loader.addResourceStore(store3);
        loader.addResourceStore(store4);

        checkRemoveResourceStore("One: Remove Store 1", loader, store1, true);
        checkRemoveResourceStore("One: Store 1 Not Found", loader, store1, false);

        checkRemoveResourceStore("One: Remove Store 2", loader, store2, true);
        checkRemoveResourceStore("One: Store 2 Not Found", loader, store2, false);

        checkRemoveResourceStore("One: Remove Store 3", loader, store3, true);
        checkRemoveResourceStore("One: Store 3 Not Found", loader, store3, false);

        checkRemoveResourceStore("One: Remove Store 4", loader, store4, true);
        checkRemoveResourceStore("One: Store 4 Not Found", loader, store4, false);
    }

    /**
     * Test trying to remove the second ResourceStore added
     *
     * Bug: ReloadingClassLoader addes ResourceStore at the start of the array. Removing the
     *      first one added (last in array) causes the second System.arraycopy() statement to throw a
     *      ArrayIndexOutOfBoundsException (??not sure why??)
     */
    public void testRemoveStoresTwo() {
        final ReloadingClassLoader loader = new ReloadingClassLoader(getClass().getClassLoader());
        loader.addResourceStore(store1);
        loader.addResourceStore(store2);
        loader.addResourceStore(store3);
        loader.addResourceStore(store4);

        checkRemoveResourceStore("Two: Remove Store 2", loader, store2, true);
        checkRemoveResourceStore("Two: Store 2 Not Found", loader, store2, false);

        checkRemoveResourceStore("Two: Remove Store 4", loader, store4, true);
        checkRemoveResourceStore("Two: Store 4 Not Found", loader, store4, false);

        checkRemoveResourceStore("Two: Remove Store 3", loader, store3, true);
        checkRemoveResourceStore("Two: Store 3 Not Found", loader, store3, false);

        checkRemoveResourceStore("Two: Remove Store 1", loader, store1, true);
        checkRemoveResourceStore("Two: Store 1 Not Found", loader, store1, false);
    }

    /**
     * Test trying to remove the third ResourceStore added
     *
     * Bug: In this scenario the two System.arraycopy() statements don't copy the correct
     *      ResourceStore - it creates a new array where the first resource store is null
     *      and copies store3 and store2 to their same positions
     */
    public void testRemoveStoresThree() {
        final ReloadingClassLoader loader = new ReloadingClassLoader(getClass().getClassLoader());
        loader.addResourceStore(store1);
        loader.addResourceStore(store2);
        loader.addResourceStore(store3);
        loader.addResourceStore(store4);

        checkRemoveResourceStore("Three: Remove Store 3", loader, store3, true);
        checkRemoveResourceStore("Three: Store 3 Not Found", loader, store3, false);

        checkRemoveResourceStore("Three: Remove Store 1", loader, store1, true);
        checkRemoveResourceStore("Three: Store 1 Not Found", loader, store1, false);

        checkRemoveResourceStore("Three: Remove Store 4", loader, store4, true);
        checkRemoveResourceStore("Three: Store 4 Not Found", loader, store4, false);

        checkRemoveResourceStore("Three: Remove Store 2", loader, store2, true);
        checkRemoveResourceStore("Three: Store 2 Not Found", loader, store2, false);
    }

    /**
     * Test trying to remove the fourth ResourceStore added
     *
     * Bug: ReloadingClassLoader addes ResourceStore at the start of the array. Removing the
     *      last one added (first in array) causes the first System.arraycopy() statement to throw a
     *      ArrayIndexOutOfBoundsException because the length to copy is -1
     */
    public void testRemoveStoresFour() {
        final ReloadingClassLoader loader = new ReloadingClassLoader(getClass().getClassLoader());
        loader.addResourceStore(store1);
        loader.addResourceStore(store2);
        loader.addResourceStore(store3);
        loader.addResourceStore(store4);

        checkRemoveResourceStore("Four: Remove Store 4", loader, store4, true);
        checkRemoveResourceStore("Four: Store 4 Not Found", loader, store4, false);

        checkRemoveResourceStore("Four: Remove Store 3", loader, store3, true);
        checkRemoveResourceStore("Four: Store 3 Not Found", loader, store3, false);

        checkRemoveResourceStore("Four: Remove Store 2", loader, store2, true);
        checkRemoveResourceStore("Four: Store 2 Not Found", loader, store2, false);

        checkRemoveResourceStore("Four: Remove Store 1", loader, store1, true);
        checkRemoveResourceStore("Four: Store 1 Not Found", loader, store1, false);
    }

    /**
     * Test that a class can't be loaded after the ResourceStore containing
     * it has been removed.
     *
     * Bug: When theres a single ResourceStore in the ClassLoader and its removed
     *      a new "delegate" ClassLoader with the new ResourceStore array isn't being
     *      created - which means that calling loadClass() still returns the classes
     *      from the removed ResourceStore rather than throwing a ClassNotFoundException
     */
    public void testLoadClassAfterResourceStoreRemoved() {

        // Create a class loader & add resource store
        final ReloadingClassLoader loader = new ReloadingClassLoader(this.getClass().getClassLoader());
        final MemoryResourceStore store = new MemoryResourceStore();
        loader.addResourceStore(store);

        // Check "jci2.Simple" class can't be loaded
        try {
            loader.loadClass("jci2.Simple").getConstructor().newInstance();
            fail("Success loadClass[1]");
        } catch (final ClassNotFoundException e) {
            // expected not found
        } catch (final Exception e) {
            log.error(e);
            fail("Error loadClass[1]: " + e);
        }

        // Add "jci2.Simple" class to the resource store
        final String toStringValue = "FooBar";
        try {
            final byte[] classBytes = SimpleDump.dump(toStringValue);
            store.write("jci2/Simple.class", classBytes);
        } catch (final Exception e) {
            log.error(e);
            fail("Error adding class to store: " + e);
        }

        // Check "jci2.Simple" class can now be loaded
        try {
            final Object simple2 = loader.loadClass("jci2.Simple").getConstructor().newInstance();
            assertNotNull("Found loadClass[2]",  simple2);
            assertEquals("toString loadClass[2]",  toStringValue, simple2.toString());
        } catch (final Exception e) {
            log.error(e);
            fail("Error loadClass[2]: " + e);
        }

        // Remove the resource store from the class loader
        checkRemoveResourceStore("Remove Resource Store", loader, store, true);

        // Test "jci2.Simple" class can't be loaded after ResourceStore removed
        try {
            loader.loadClass("jci2.Simple").getConstructor().newInstance();
            fail("Success loadClass[3]");
        } catch (final ClassNotFoundException e) {
            // expected not found
        } catch (final Exception e) {
            log.error(e);
            fail("Error loadClass[3]: " + e);
        }

    }

    /**
     * Check removing a ResourceStore from ReloadingClassLoader
     */
    private void checkRemoveResourceStore(final String label, final ReloadingClassLoader loader, final ResourceStore store, final boolean expected) {
        try {
            assertEquals(label, expected, loader.removeResourceStore(store));
        } catch (final Exception e) {
            log.error(label, e);
            fail(label + " failed: " + e);
        }
    }
}
