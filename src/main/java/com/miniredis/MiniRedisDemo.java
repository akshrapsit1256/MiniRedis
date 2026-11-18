package com.miniredis;

import java.util.ArrayList;

public class MiniRedisDemo {

    public static void main(String[] args) throws InterruptedException {

        KeyValueStore store = new KeyValueStore();

        try {
            // 1. Basic CRUD operations
            System.out.println("=== CRUD OPERATIONS ===");

            store.set("User1", "Akshra");
            store.set("User2", "Anushka");

            System.out.println("User1: " + store.get("User1"));

            store.delete("User1");

            System.out.println("User1 exists: " + store.exists("User1"));


            // 2. Thread safety
            System.out.println("\n=== THREAD SAFETY ===");

            Thread t2 = new Thread(() -> {
                store.set("User4", "Shyam");
            });

            t2.start();
            t2.join();

            System.out.println("User4: " + store.get("User4"));


            // 3. Concurrent increment
            System.out.println("\n=== CONCURRENT INCREMENT ===");

            store.set("Counter", "0");

            ArrayList<Thread> threads = new ArrayList<>();

            for (int i = 1; i <= 100; i++) {
                Thread t = new Thread(() -> store.increment("Counter"));

                threads.add(t);
                t.start();
            }

            for (Thread t : threads) {
                t.join();
            }

            System.out.println("Final Counter: " + store.get("Counter"));


            // 4. TTL expiration
            System.out.println("\n=== TTL EXPIRATION ===");

            store.set("Temp", "hello", 2000);

            System.out.println("Before expiration: " + store.get("Temp"));

            Thread.sleep(2500);

            System.out.println("After expiration: " + store.get("Temp"));


            // 5. Normal SET clears previous TTL
            System.out.println("\n=== TTL RESET ===");

            store.set("Temp", "hello", 2000);
            store.set("Temp", "world");

            Thread.sleep(2500);

            System.out.println("After previous TTL: " + store.get("Temp"));


            // 6. Sequential TTL reset
            System.out.println("\n=== SEQUENTIAL TTL RESET ===");

            store.set("ResetTest", "old", 2000);

            Thread.sleep(1000);

            store.set("ResetTest", "new", 4000);

            Thread.sleep(1500);

            System.out.println("Updated value: " + store.get("ResetTest"));


            // 7. Background cleanup
            System.out.println("\n=== BACKGROUND CLEANUP ===");

            store.set("UserNew", "AkshraP", 2000);

            System.out.println("Before expiration: " + store.exists("User Ji"));

            Thread.sleep(3000);

            System.out.println("After expiration: " + store.exists("User Ji"));

        } finally {
            store.shutdown();
        }
    }
}