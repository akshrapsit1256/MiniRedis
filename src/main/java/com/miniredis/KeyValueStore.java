package com.miniredis;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeyValueStore {

    // ==========================================
    // 1. DATA STORAGE
    // ==========================================

    private final ConcurrentHashMap<String, CacheEntry> data =
            new ConcurrentHashMap<>();

    // TTLManager ttlManager = new TTLManager();


    // ==========================================
    // 2. BACKGROUND EXPIRATION SCHEDULER
    // ==========================================

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });

    // Constructor: Start automatic cleanup
    public KeyValueStore() {
    scheduler.scheduleAtFixedRate(
            () -> {
                try {
                    cleanupExpiredKeys();
                } catch (Exception e) {
                    System.err.println(
                            "Background cleanup failed: " + e.getMessage()
                    );
                    e.printStackTrace();
                }
            },
            1,
            1,
            TimeUnit.SECONDS
    );
}

    // Stop the scheduler
    public void shutdown() {
        scheduler.shutdown();
    }


    // ==========================================
    // 3. BASIC CRUD OPERATIONS
    // ==========================================

    // SET: Store a key without expiration
    public void set(String key, String value) {
        // ttlManager.expiration.remove(key);
        data.put(key, new CacheEntry(value, null));
    }

    // GET: Retrieve a value using its key
    public String get(String key) {
        CacheEntry entry = data.get(key);

        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            data.remove(key, entry);
            return null;
        }

        return entry.getValue();
    }

    // DELETE: Remove a key from storage
    public void delete(String key) {
        data.remove(key);
    }

    // EXISTS: Check whether a key exists
    public boolean exists(String key) {
        CacheEntry entry = data.get(key);

        if (entry == null) {
            return false;
        }

        if (entry.isExpired()) {
            data.remove(key, entry);
            return false;
        }

        return true;
    }


    // ==========================================
    // 4. TTL OPERATIONS
    // ==========================================

    // SET WITH TTL: Store a key with an expiration time
    public void set(String key, String value, long ttl) {
        if (ttl <= 0) {
            throw new IllegalArgumentException("TTL must be greater than 0");
        }

        if (ttl > Long.MAX_VALUE - System.currentTimeMillis()) {
            throw new IllegalArgumentException("TTL is too large");
        }

        Long expirationTime = System.currentTimeMillis() + ttl;
        data.put(key, new CacheEntry(value, expirationTime));
    }

    // TTL: Get the remaining time before expiration
    public long ttl(String key) {
        CacheEntry entry = data.get(key);

        if (entry == null) {
            return -2;
        }

        if (entry.isExpired()) {
            data.remove(key, entry);
            return -2;
        }

        return entry.remainingTTL();
    }


    // ==========================================
    // 5. ATOMIC OPERATIONS
    // ==========================================

    // INCREMENT: Atomically increase the stored numeric value
    // INCREMENT: Atomically increase the stored numeric value
public void increment(String key) {
    data.compute(key, (k, entry) -> {

        // Create a new key if missing or expired
        if (entry == null || entry.isExpired()) {
            return new CacheEntry("1", null);
        }

        // Parse the existing value as long
        long currentValue = Long.parseLong(entry.getValue());

        // Safely increment the value
        long nextValue = Math.addExact(currentValue, 1);

        return new CacheEntry(
                String.valueOf(nextValue),
                entry.getExpirationTime()
        );
    });
}


    // ==========================================
    // 6. BACKGROUND CLEANUP
    // ==========================================

    // Remove expired keys automatically
    public void cleanupExpiredKeys() {
        for (Map.Entry<String, CacheEntry> entry : data.entrySet()) {

            String key = entry.getKey();
            CacheEntry cacheEntry = entry.getValue();

            if (cacheEntry.isExpired()) {
                if (data.remove(key, cacheEntry)) {
                    System.out.println("Expire Key Found: " + key);
                }
            }
        }
    }


    // ==========================================
    // PHASE 4 - SNAPSHOT
    // ==========================================
    public ConcurrentHashMap<String,CacheEntry> getSnapshot(){
        return new ConcurrentHashMap<>(data);
    }

    // ==========================================
    // 7. MAIN METHOD / TESTING
    // ==========================================

    // public static void main(String[] args) throws InterruptedException {

    //     KeyValueStore store = new KeyValueStore();

    //     // Basic CRUD testing
    //     store.set("User1", "Akshra");
    //     store.set("User2", "Anushka");

    //     System.out.println(store.get("User1"));

    //     store.delete("User1");

    //     System.out.println(store.exists("User1"));


    //     // Thread safety testing
    //     Thread t2 = new Thread(() -> {
    //         store.set("User4", "Shyam");
    //     });

    //     t2.start();
    //     t2.join();


    //     // Concurrent increment testing
    //     store.set("Counter", "0");

    //     ArrayList<Thread> threads = new ArrayList<>();

    //     for (int i = 1; i <= 100; i++) {
    //         Thread t = new Thread(() -> {
    //             store.increment("Counter");
    //         });

    //         threads.add(t);
    //         t.start();
    //     }

    //     for (Thread t : threads) {
    //         t.join();
    //     }

    //     System.out.println("Final Counter: " + store.get("Counter"));
    //     System.out.println(store.get("User4"));


    //     // TTL testing
    //     store.set("Temp", "hello", 2000);

    //     System.out.println(store.get("Temp"));

    //     Thread.sleep(2500);

    //     System.out.println(store.get("Temp"));


    //     // Normal SET should clear the previous TTL
    //     store.set("Temp", "hello", 2000);
    //     store.set("Temp", "world");

    //     Thread.sleep(2500);

    //     System.out.println(store.get("Temp"));


    //     // Sequential TTL reset testing
    //     store.set("ResetTest", "old", 2000);

    //     Thread.sleep(1000);

    //     store.set("ResetTest", "new", 4000);

    //     Thread.sleep(1500);

    //     System.out.println(store.get("ResetTest"));


    //     // Background expiration testing
    //     store.set("User Ji", "Akshra Jii", 2000);

    //     System.out.println(store.exists("User Ji"));

    //     Thread.sleep(3000);

    //     System.out.println(store.data.containsKey("User Ji"));


    //     // Shutdown background scheduler
    //     store.shutdown();
    // }
}