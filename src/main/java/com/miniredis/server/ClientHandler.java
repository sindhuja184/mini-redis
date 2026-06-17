package com.miniredis.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.miniredis.protocol.RespParser;
import com.miniredis.protocol.RespWriter;
import com.miniredis.command.CommandHandler;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    private final CommandHandler commandHandler;

    public ClientHandler(Socket clientSocket, CommandHandler commandHandler) {
        this.clientSocket = clientSocket;
        this.commandHandler = commandHandler;
    }

    @Override
    public void run() {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {

            while (true) {
                String[] tokens = RespParser.parse(inputStream);

                if (tokens == null) {
                    break;
                }

                System.out.println("Received");

                for (String token : tokens) {
                    System.out.println(token);
                }

                String response = commandHandler.handle(tokens);

                outputStream.write(response.getBytes());
                outputStream.flush();
            }

        } catch (IOException e) {
            System.err.println("Error handling client " + clientSocket.getRemoteSocketAddress() + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println(
                        "Client disconnected: "
                                + clientSocket.getRemoteSocketAddress()
                );
            } catch (IOException ignored) {
            }
        }
    }
}