# Mini-Redis

A lightweight, multi-threaded implementation of a Redis-compatible TCP server written in Java. It speaks standard RESP (REdis Serialization Protocol) and is compatible with official Redis client utilities (`redis-cli`, `redis-benchmark`).

---

## ✨ Features

- **Multi-threaded TCP Server**: Handled concurrently via a configurable thread pool executor.
- **RESP Protocol Engine**: Custom-built parser and writer supporting RESP bulk strings, simple strings, integers, errors, and arrays.
- **LRU Cache Eviction**: Configurable cache limit (`MAX_KEYS`) that automatically evicts the least recently used keys using an access-ordered map.
- **TTL (Time To Live) Support**:
  - Key expiration using passive expiration checks (checked on read).
  - Active background cleanup task to prune expired keys periodically.
- **Supported Commands**:
  - **Core**: `SET`, `GET`, `DEL`, `EXISTS`, `PING`, `ECHO`
  - **TTL & Expiry**: `EXPIRE`, `TTL`, `PERSIST`

---

## 📁 Project Structure

The project layout follows a standard Maven Java structure:

*   **[App.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/App.java)**: Main entry point initializing and starting the TCP server.
*   **[Config.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/Config.java)**: Central configuration properties (port, thread pool size, cache limit).
*   **[TcpServer.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/server/TcpServer.java)**: Listens for incoming client socket connections and delegates them.
*   **[ClientHandler.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/server/ClientHandler.java)**: Processes individual client connections, reading and responding to RESP commands sequentially.
*   **[DataStore.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/store/DataStore.java)**: Core thread-safe memory manager implementing LRU eviction and TTL scheduling.

---

## 🛠️ Getting Started

### Prerequisites
- JDK 8 or higher
- Apache Maven

### 1. Compile the Project
```bash
mvn compile
```

### 2. Run the Server (Maven Exec)
```bash
mvn exec:java -Dexec.mainClass="com.miniredis.App"
```

### 3. Build & Package the Jar
```bash
mvn clean package
```

### 4. Run the Server (Jar Entry)
```bash
java -cp target/mini-redis-1.0-SNAPSHOT.jar com.miniredis.App
```

---

## ⚡ Performance & Benchmarking

The server has been optimized to run without verbose logging bottlenecks and achieves high-performance throughput:

- **Hardware**: Standard Dev Machine (Windows)
- **Tool**: Official `redis-benchmark` utility
- **Throughput**:
  - **SET**: **~21,900 ops/second** (10 parallel clients)
  - **GET**: **~22,060 ops/second** (10 parallel clients)
  - **SET**: **~17,960 ops/second** (50 parallel clients)
  - **GET**: **~14,640 ops/second** (50 parallel clients)

To run the benchmarks yourself, check the instructions in the [benchmarking_report.md](file:///C:/Users/sindh/.gemini/antigravity-ide/brain/ac12ef2e-222b-417a-83fe-fa52a4e2d3c6/benchmarking_report.md) artifact.