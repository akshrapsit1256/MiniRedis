# Mini Redis 🚀

A lightweight, thread-safe, in-memory key-value store built from scratch using Java.

I started building Mini Redis to understand how key-value storage systems like Redis work internally and to explore the backend engineering concepts behind them.

Instead of building everything at once, I'm developing this project phase by phase. Each phase focuses on a different concept, from basic data storage and concurrency to persistence and distributed systems.

The project is still under development, and I'll keep adding features as I move forward.

## Features Implemented

### 1. Basic CRUD Operations
- Store key-value pairs.
- Retrieve values using keys.
- Delete existing keys.
- Check whether a key exists.

### 2. Thread Safety
- Uses Java's `ConcurrentHashMap` for thread-safe storage.
- Supports concurrent access from multiple threads.
- Uses atomic map operations to avoid lost updates.

### 3. TTL (Time To Live)
- Assign expiration times to individual keys.
- Automatically expire keys after their TTL.
- Reset expiration when an existing key is updated.
- Support permanent keys without expiration.
- Check the remaining TTL of stored keys.

### 4. Background Expiration Cleanup
- Uses `ScheduledExecutorService` for periodic cleanup.
- Automatically scans and removes expired entries.
- Uses conditional removal to prevent cleanup from deleting newer values.

### 5. Atomic Increment
- Implements thread-safe counter updates using `ConcurrentHashMap.compute()`.
- Supports Java `long` values.
- Preserves existing TTL during increment operations.
- Detects numeric overflow.

## 🛠️ Tech Stack

- **Language:** Java 17
- **Build Tool:** Apache Maven
- **Testing:** JUnit 5
- **Concurrency:** ConcurrentHashMap, Threads, ScheduledExecutorService
- **Version Control:** Git & GitHub

## 📂 Project Structure

```text
MiniRedis/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── miniredis/
│   │               ├── CacheEntry.java
│   │               ├── KeyValueStore.java
│   │               └── MiniRedisDemo.java
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── miniredis/
│                   └── ConcurrentTTLTests.java
│
├── pom.xml
├── README.md
└── .gitignore
```

## 🚀 Getting Started

How to run Mini Redis Locally ?

### Prerequisites

Make sure you have installed:
- Java 17 or later
- Apache Maven
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/akshrapsit1256/MiniRedis.git
```

### 2. Navigate to the Project

```bash
cd MiniRedis
```

### 3. Compile the Project

```bash
mvn compile
```

### 4. Run the Demonstration

```bash
java -cp target/classes com.miniredis.MiniRedisDemo
```

### 5. Run Automated Tests

```bash
mvn test
```

## 💻 Example Usage

Here's a small example of how the current key-value store works:

```java
KeyValueStore store = new KeyValueStore();

try {
    // Store a permanent key
    store.set("name", "MiniRedis");

    // Retrieve a value
    System.out.println(store.get("name"));

    // Store a key with a 5-second TTL
    store.set("session", "active", 5000);

    // Check remaining TTL
    System.out.println(store.ttl("session"));

    // Increment a counter
    store.set("counter", "10");
    store.increment("counter");

    System.out.println(store.get("counter"));

    // Delete a key
    store.delete("name");

} finally {
    store.shutdown();
}
```

## 🧪 Testing

I'm using JUnit 5 to test the functionality and reliability of Mini Redis.

**Current test coverage: 15 automated test cases.**

The test suite covers:

1. Concurrent updates to the same key.
2. Concurrent TTL reset.
3. Key expiration after TTL.
4. Normal SET clearing an existing TTL.
5. Permanent keys returning `-1` TTL.
6. Missing or expired keys returning `-2` TTL.
7. Deleting keys with TTL.
8. Background expiration cleanup.
9. Increment operations preserving existing TTL.
10. Incrementing expired keys.
11. Rejection of invalid TTL values.
12. Concurrent counter increments without lost updates.
13. Support for large `long` counter values.
14. Counter overflow detection.
15. Rejection of non-numeric counter values.

Run the test suite using:

```bash
mvn test
```

All 15 test methods currently pass.

## 📌 Development Roadmap

🚧 **Status: Currently in Development**

I'm building Mini Redis in multiple phases. Here's the progress so far and what I plan to work on next.

| Phase | Description | Status |
|---|---|---|
| Phase 1 | Basic CRUD Operations | ✅ Completed |
| Phase 2 | Thread Safety & Concurrent Access | ✅ Completed |
| Phase 3 | TTL & Automatic Key Expiration | ✅ Completed |
| Phase 4 | Data Persistence & Recovery | ⬜ Planned |
| Phase 5 | LRU Cache Eviction | ⬜ Planned |
| Phase 6 | TCP Networking & Client-Server Communication | ⬜ Planned |
| Phase 7 | Distributed Architecture | ⬜ Planned |
| Phase 8 | Replication & Fault Tolerance | ⬜ Planned |
| Phase 9 | Performance Benchmarking & Optimization | ⬜ Planned |

### What's Coming Next?

A quick look at the upcoming phases:

- **Persistence:** Make stored data survive application restarts.
- **LRU Eviction:** Introduce a cache eviction strategy to manage memory.
- **Networking:** Allow clients to communicate with Mini Redis over TCP.
- **Distributed Architecture:** Explore how multiple storage nodes can work together.
- **Replication:** Improve availability and fault tolerance.
- **Benchmarking:** Measure performance and identify bottlenecks.

I'll update the roadmap as these features are implemented and tested.

## 🎯 What I'm Learning

While working on this project, I'm exploring:

- How in-memory key-value stores work.
- Thread safety and concurrent programming in Java.
- Atomic operations and race-condition prevention.
- TTL-based expiration and memory management.
- Background task scheduling.
- Cache eviction strategies.
- Data persistence and recovery.
- Networking and distributed systems.

## ⚠️ Disclaimer

Mini Redis is an educational project built to explore backend engineering and storage system concepts.

It is not a production-ready or Redis-compatible implementation.

---

## 👩‍💻 Author

**Akshra**

- GitHub: [@akshrapsit1256](https://github.com/akshrapsit1256)
- B.Tech CSE Student | Java & Backend Development
- Interested in Data Structures, Algorithms, and System Design.

**Built with Java, curiosity, and a lot of debugging.** 💻
