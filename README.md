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
- **AOF (Append Only File) Persistence & Recovery**:
  - Logs all state-mutating commands (like `SET`, `DEL`, `EXPIRE`, `PERSIST`, `FLUSHALL`) to `appendonly.aof`.
  - Automatically restores server state on startup by replaying the command history from the AOF log file.

---

## 📁 Project Structure

The project layout follows a standard Maven Java structure:

*   **[App.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/App.java)**: Main entry point initializing and starting the TCP server, recovering state from the AOF log.
*   **[Config.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/Config.java)**: Central configuration properties (port, thread pool size, cache limit).
*   **[TcpServer.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/server/TcpServer.java)**: Listens for incoming client socket connections and delegates them.
*   **[ClientHandler.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/server/ClientHandler.java)**: Processes individual client connections, reading and responding to RESP commands sequentially.
*   **[DataStore.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/store/DataStore.java)**: Core thread-safe memory manager implementing LRU eviction and TTL scheduling.
*   **[CommandHandler.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/command/CommandHandler.java)**: Dispatches and validates incoming commands, writing modifying commands to AOF and interacting with the data store.
*   **[AofManager.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/manager/AofManager.java)**: Manages sequential writing of mutating commands to the append-only log file.
*   **[AofRecoveryManager.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/manager/AofRecoveryManager.java)**: Parses the append-only log at startup and replays commands to restore state.
*   **[RespParser.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/protocol/RespParser.java)**: Parse logic to read incoming RESP data arrays from input streams.
*   **[RespWriter.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/protocol/RespWriter.java)**: Formatting utility to write RESP responses (OK, error, integer, bulkString, array) to client output streams.
*   **[test-redis.ps1](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/test-redis.ps1)**: A helper PowerShell script to connect to the server and execute commands from the terminal.

---

## 🚀 Supported Commands

### Core
*   **`PING`**
    *   *Usage*: `PING`
    *   *Returns*: `+PONG\r\n`
*   **`SET`**
    *   *Usage*: `SET <key> <value>`
    *   *Returns*: `+OK\r\n` (Note: Overwriting a key clears any associated TTL/expiry).
*   **`GET`**
    *   *Usage*: `GET <key>`
    *   *Returns*: Bulk string value of the key, or `$-1\r\n` (nil) if the key does not exist or has expired.
*   **`DEL`**
    *   *Usage*: `DEL <key> [key ...]`
    *   *Returns*: Long integer response indicating the number of keys successfully deleted.
*   **`EXISTS`**
    *   *Usage*: `EXISTS <key>`
    *   *Returns*: Integer response `1` if key exists, `0` otherwise.
*   **`FLUSHALL`**
    *   *Usage*: `FLUSHALL`
    *   *Returns*: `+OK\r\n` (Clears all stored data and active TTL tracking).

### TTL & Expiry
*   **`EXPIRE`**
    *   *Usage*: `EXPIRE <key> <seconds>`
    *   *Returns*: Integer response `1` if expiry was set successfully, `0` if the key does not exist or is expired.
*   **`TTL`**
    *   *Usage*: `TTL <key>`
    *   *Returns*: Integer response of remaining TTL in seconds. Returns `-1` if the key has no TTL, and `-2` if the key does not exist.
*   **`PERSIST`**
    *   *Usage*: `PERSIST <key>`
    *   *Returns*: Integer response `1` if the timeout was removed, `0` if the key does not exist or does not have an active expiry.

---

## 🛠️ Getting Started

### Prerequisites
- JDK 8 or higher
- Apache Maven
- PowerShell (optional, for testing commands on Windows)

### 1. Compile the Project
```bash
mvn compile
```

### 2. Run Unit Tests
```bash
mvn test
```

### 3. Run the Server (Maven Exec)
For **Bash**:
```bash
mvn exec:java -Dexec.mainClass="com.miniredis.App"
```
For **PowerShell**:
```powershell
mvn exec:java "-Dexec.mainClass=com.miniredis.App"
```

### 4. Build & Package the Jar
```bash
mvn clean package
```

### 5. Run the Server (Jar Entry)
```bash
java -cp target/mini-redis-1.0-SNAPSHOT.jar com.miniredis.App
```

### 6. Connect and Test Commands
You can connect using standard `redis-cli`:
```bash
redis-cli -p 6379
```
If you are on Windows and don't have `redis-cli` installed, you can use the bundled PowerShell client script:
```powershell
.\test-redis.ps1 SET name Sindhuja
.\test-redis.ps1 GET name
.\test-redis.ps1 EXPIRE name 60
.\test-redis.ps1 TTL name
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