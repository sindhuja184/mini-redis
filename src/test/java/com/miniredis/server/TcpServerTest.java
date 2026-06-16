package com.miniredis.server;

import org.junit.Test;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import static org.junit.Assert.*;

public class TcpServerTest {

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    public void testServerPingAndPersistence() throws Exception {
        final int port = findFreePort();
        final TcpServer server = new TcpServer(port);

        // Start server in a background thread
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                server.start();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Give server a moment to start
        Thread.sleep(500);

        // Connect client
        try (Socket client = new Socket("localhost", port)) {
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();

            // Send PING
            out.write("*1\r\n$4\r\nPING\r\n".getBytes());
            out.flush();

            // Read response
            byte[] responseBuffer = new byte[1024];
            int read = in.read(responseBuffer);
            assertTrue(read > 0);
            String response = new String(responseBuffer, 0, read);
            assertEquals("+PONG\r\n", response);

            // Send another PING to test persistence
            out.write("*1\r\n$4\r\nPING\r\n".getBytes());
            out.flush();

            read = in.read(responseBuffer);
            assertTrue(read > 0);
            response = new String(responseBuffer, 0, read);
            assertEquals("+PONG\r\n", response);

            // Send an unknown command
            out.write("*2\r\n$4\r\nECHO\r\n$5\r\nhello\r\n".getBytes());
            out.flush();

            read = in.read(responseBuffer);
            assertTrue(read > 0);
            response = new String(responseBuffer, 0, read);
            assertTrue(response.startsWith("-ERR unknown command"));
        }
    }
}
