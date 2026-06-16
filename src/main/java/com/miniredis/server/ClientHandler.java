package com.miniredis.server;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

        try {

            System.out.println(
                    "Handling client: "
                            + clientSocket.getRemoteSocketAddress()
            );

            // RESP parsing will come here later

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