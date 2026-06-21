package com.miniredis;

import com.miniredis.command.CommandHandler;
import com.miniredis.manager.AofManager;
import com.miniredis.manager.AofRecoveryManager;
import com.miniredis.server.TcpServer;
import com.miniredis.store.DataStore;


public class App {
    public static void main( String[] args ) throws Exception{
        DataStore dataStore = new DataStore();

        AofManager aofManager = new AofManager("appendonly.aof");

        CommandHandler commandHandler = new CommandHandler(dataStore, aofManager);

        AofRecoveryManager recoveryManager = new AofRecoveryManager(commandHandler);

        recoveryManager.recover("appendonly.aof");

        TcpServer tcpServer = new TcpServer(Config.PORT, commandHandler);
        tcpServer.start();

    }
}
