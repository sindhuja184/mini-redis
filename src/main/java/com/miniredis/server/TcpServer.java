package com.miniredis.server;

import com.miniredis.Config;
import com.miniredis.command.CommandHandler;
import com.miniredis.store.DataStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer {

    private final int port;
    private final ExecutorService threadPool;
    private final CommandHandler commandHandler;

    public TcpServer(int port) {
        this(port, new CommandHandler(new DataStore()));
    }

    public TcpServer(int port,
                     CommandHandler commandHandler) {

        this.port = port;
        this.commandHandler = commandHandler;

        this.threadPool =
                Executors.newFixedThreadPool(
                        Config.THREAD_POOL_SIZE
                );
    }

    public void start() {
        try(ServerSocket serverSocket =
                    new ServerSocket(port)) {

            System.out.println(
                    "Mini Redis started on port "
                            + port);

            while(true) {

                Socket clientSocket =
                        serverSocket.accept();

                System.out.println(
                        "Client Connected: "
                                + clientSocket.getRemoteSocketAddress());

                threadPool.submit(
                        new ClientHandler(
                                clientSocket,
                                commandHandler
                        )
                );
            }

        } catch(IOException e) {

            System.err.println(
                    "Server error: "
                            + e.getMessage());

            e.printStackTrace();
        }
    }
}