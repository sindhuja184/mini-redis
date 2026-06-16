# Mini Redis

A lightweight, multi-threaded implementation of a Redis-compatible TCP server written in Java.

## Project Structure

The project layout follows a standard Maven Java structure:

*   **[App.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/App.java)**: The main entry point that initializes and starts the TCP server.
*   **[Config.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/Config.java)**: Contains configuration constants (e.g., `PORT` = `6379`, `THREAD_POOL_SIZE` = `10`).
*   **[TcpServer.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/server/TcpServer.java)**: A multi-threaded TCP server listening for client connections and submitting connection handlers to an executor pool.
*   **[ClientHandler.java](file:///c:/Users/sindh/OneDrive/Desktop/project/mini-redis/src/main/java/com/miniredis/server/ClientHandler.java)**: Implements `Runnable` to process client requests concurrently.

---

## Commands

Here are the Maven and Java commands used for managing, building, and running this project.

### 1. Compile the Project
To compile the Java source files into classes under the `target/` directory:
```bash
mvn compile
```

### 2. Run the Server
To execute the main class and start the TCP server:
```bash
mvn exec:java -Dexec.mainClass="com.miniredis.App"
```

### 3. Clean the Build Directory
To delete the `target/` directory and all build artifacts:
```bash
mvn clean
```

### 4. Build/Package the JAR
To compile the code, run any tests, and package the classes into a redistributable JAR file:
```bash
mvn package
```

### 5. Run the Server from the Packaged JAR
Once compiled and packaged, you can run the server directly using the Java VM:
```bash
java -cp target/mini-redis-1.0-SNAPSHOT.jar com.miniredis.App
```
