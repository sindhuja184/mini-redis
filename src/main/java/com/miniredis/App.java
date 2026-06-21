package com.miniredis;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.miniredis.command.CommandHandler;
import com.miniredis.manager.AofManager;
import com.miniredis.manager.AofRecoveryManager;
import com.miniredis.manager.RdbManager;
import com.miniredis.server.TcpServer;
import com.miniredis.store.DataStore;


public class App {
    public static void main( String[] args ) throws Exception{
        DataStore dataStore = new DataStore();

        AofManager aofManager = new AofManager("appendonly.aof");

        CommandHandler commandHandler = new CommandHandler(dataStore, aofManager);

        AofRecoveryManager recoveryManager = new AofRecoveryManager(commandHandler);

        RdbManager rbdManager = new RdbManager("dump.rdb");

        try {
            rbdManager.load(dataStore);
            System.out.println("RDB snapshot loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading RDB snapshot: " + e.getMessage());
        }

        recoveryManager.recover("appendonly.aof");

        ScheduledExecutorService snapshotScheduler = Executors.newSingleThreadScheduledExecutor();

        snapshotScheduler.scheduleAtFixedRate(
            () -> {
                try {
                    rbdManager.save(dataStore);
                    System.out.println("RDB snapshot saved successfully");
                } catch (Exception e) {
                    System.err.println("Error saving RDB snapshot: " + e.getMessage());
                }
            }, 
            5, 
            5, 
            TimeUnit.SECONDS
        );

        TcpServer tcpServer = new TcpServer(Config.PORT, commandHandler);
        tcpServer.start();

    }
}
