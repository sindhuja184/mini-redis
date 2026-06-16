package com.miniredis;

import com.miniredis.server.TcpServer;


public class App {
    public static void main( String[] args ) {

        TcpServer server = new TcpServer(Config.PORT);
        server.start();
    }
}
