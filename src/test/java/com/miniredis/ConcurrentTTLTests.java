package com.miniredis;

import org.junit.jupiter.api.Test;

// import com.miniredis.KeyValueStore;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

class ConcurrentTTLTests {

    // ==========================================
    // TEST 1: CONCURRENT UPDATES TO SAME KEY
    // ==========================================

    @Test
    void concurrentUpdatesShouldStoreOneOfTheLatestValues()
            throws InterruptedException {

        KeyValueStore store = new KeyValueStore();

        try {
            store.set("user", "Initial");

            int numberOfThreads = 10;

            CountDownLatch ready = new CountDownLatch(numberOfThreads);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(numberOfThreads);

            ArrayList<Thread> threads = new ArrayList<>();
            Set<String> expectedValues = new HashSet<>();

            for (int i = 1; i <= numberOfThreads; i++) {

                String value = "User" + i;
                expectedValues.add(value);

                Thread thread = new Thread(() -> {
                    try {
                        ready.countDown();
                        start.await();

                        store.set("user", value);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();

                    } finally {
                        done.countDown();
                    }
                });

                threads.add(thread);
                thread.start();
            }

            ready.await();
            start.countDown();
            done.await();

            assertTrue(expectedValues.contains(store.get("user")));

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 2: CONCURRENT TTL RESET
    // ==========================================

    @Test
    void concurrentTTLResetShouldPreserveNewValue()
            throws InterruptedException {

        KeyValueStore store = new KeyValueStore();

        try {
            store.set("user", "Akshra", 1500);

            Thread.sleep(1000);

            Thread updater = new Thread(
                    () -> store.set("user", "Anushka", 5000)
            );

            updater.start();
            updater.join();

            Thread.sleep(1200);

            assertEquals("Anushka", store.get("user"));

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 3: KEY SHOULD EXPIRE AFTER TTL
    // ==========================================

    @Test
    void keyShouldExpireAfterTTL() throws InterruptedException {

        KeyValueStore store = new KeyValueStore();

        try {
            store.set("temp", "hello", 200);

            assertEquals("hello", store.get("temp"));

            Thread.sleep(300);

            assertNull(store.get("temp"));

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 4: NORMAL SET SHOULD CLEAR TTL
    // ==========================================

    @Test
    void normalSetShouldClearExistingTTL()
            throws InterruptedException {

        KeyValueStore store = new KeyValueStore();

        try {
            store.set("user", "Old", 200);

            store.set("user", "New");

            Thread.sleep(300);

            assertEquals("New", store.get("user"));
            assertEquals(-1, store.ttl("user"));

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 5: PERMANENT KEY SHOULD RETURN -1
    // ==========================================

    @Test
    void permanentKeyShouldReturnNegativeOne() {

        KeyValueStore store = new KeyValueStore();

        try {
            store.set("user", "Akshra");

            assertEquals(-1, store.ttl("user"));

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 6: MISSING OR EXPIRED KEY SHOULD RETURN -2
    // ==========================================

    @Test
    void missingKeyShouldReturnNegativeTwo()
            throws InterruptedException {

        KeyValueStore store = new KeyValueStore();

        try {
            assertEquals(-2, store.ttl("missing"));

            store.set("temp", "hello", 200);

            Thread.sleep(300);

            assertEquals(-2, store.ttl("temp"));

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 7: DELETE SHOULD REMOVE KEY AND TTL
    // ==========================================

    @Test
    void deleteShouldRemoveKeyWithTTL() {

        KeyValueStore store = new KeyValueStore();

        try {
            store.set("user", "Akshra", 5000);

            assertTrue(store.exists("user"));

            store.delete("user");

            assertFalse(store.exists("user"));
            assertNull(store.get("user"));
            assertEquals(-2, store.ttl("user"));

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 8: BACKGROUND CLEANUP
    // ==========================================

    @Test
    void cleanupShouldRemoveExpiredKeys()
            throws InterruptedException {

        KeyValueStore store = new KeyValueStore();

        try {
            store.set("temp", "hello", 200);

            Thread.sleep(300);

            store.cleanupExpiredKeys();

            assertFalse(store.exists("temp"));

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 9: INCREMENT SHOULD PRESERVE ACTIVE TTL
    // ==========================================

    @Test
    void incrementShouldPreserveExistingTTL()
            throws InterruptedException {

        KeyValueStore store = new KeyValueStore();

        try {
            store.set("counter", "10", 3000);

            store.increment("counter");

            assertEquals("11", store.get("counter"));

            assertTrue(store.ttl("counter") > 0);

            Thread.sleep(500);

            assertEquals("11", store.get("counter"));

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 10: INCREMENT EXPIRED KEY
    // ==========================================

    @Test
    void incrementExpiredKeyShouldCreateNewKey()
            throws InterruptedException {

        KeyValueStore store = new KeyValueStore();

        try {
            store.set("counter", "100", 200);

            Thread.sleep(300);

            store.increment("counter");

            assertEquals("1", store.get("counter"));
            assertEquals(-1, store.ttl("counter"));

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 11: INVALID TTL
    // ==========================================

    @Test
    void invalidTTLShouldThrowException() {

        KeyValueStore store = new KeyValueStore();

        try {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> store.set("user", "Akshra", 0)
            );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> store.set("user", "Akshra", -100)
            );

        } finally {
            store.shutdown();
        }
    }


    // ==========================================
    // TEST 12: CONCURRENT INCREMENTS
    // ==========================================

    @Test
    void concurrentIncrementsShouldNotLoseUpdates()
            throws InterruptedException {

        KeyValueStore store = new KeyValueStore();

        try {
            store.set("counter", "0");

            int numberOfThreads = 100;

            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(numberOfThreads);

            for (int i = 0; i < numberOfThreads; i++) {

                Thread thread = new Thread(() -> {
                    try {
                        start.await();
                        store.increment("counter");

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();

                    } finally {
                        done.countDown();
                    }
                });

                thread.start();
            }

            start.countDown();
            done.await();

            assertEquals("100", store.get("counter"));

        } finally {
            store.shutdown();
        }
    }

    @Test
void incrementShouldSupportLongValues() {
    KeyValueStore store = new KeyValueStore();

    try {
        store.set("counter", "3000000000");

        store.increment("counter");

        assertEquals("3000000001", store.get("counter"));

    } finally {
        store.shutdown();
    }
}
@Test
void incrementShouldThrowExceptionOnOverflow() {
    KeyValueStore store = new KeyValueStore();

    try {
        store.set("counter", String.valueOf(Long.MAX_VALUE));

        assertThrows(
                ArithmeticException.class,
                () -> store.increment("counter")
        );

        assertEquals(
                String.valueOf(Long.MAX_VALUE),
                store.get("counter")
        );

    } finally {
        store.shutdown();
    }
}
@Test
void incrementShouldRejectNonNumericValues() {
    KeyValueStore store = new KeyValueStore();

    try {
        store.set("counter", "hello");

        assertThrows(
                NumberFormatException.class,
                () -> store.increment("counter")
        );

        assertEquals("hello", store.get("counter"));

    } finally {
        store.shutdown();
    }
}
}