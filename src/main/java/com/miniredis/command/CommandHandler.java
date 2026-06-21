package com.miniredis.command;

import java.io.IOException;

import com.miniredis.manager.AofManager;
import com.miniredis.protocol.RespWriter;
import com.miniredis.store.DataStore;

public class CommandHandler {

    private final DataStore dataStore;
    private final AofManager aofManager;

    public CommandHandler(DataStore dataStore) {
        this.dataStore = dataStore;
        this.aofManager = null;
    }

    public CommandHandler(DataStore dataStore, AofManager aofManager) {
        this.dataStore = dataStore;
        this.aofManager = aofManager;
    }

    private void persists(String []tokens, boolean recoveryMode) {
        if (recoveryMode) {
            return;
        }
        if (aofManager == null) {
            return;
        }
        try {
            aofManager.append(tokens);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to persist command to AOF");
        }
    }

    //Adding overloaded method
    public String handle(String []tokens) {
        return handle(tokens, false);
    }
    public String handle(String[] tokens, boolean recoveryMode) {
        if (tokens == null || tokens.length == 0) {
            return RespWriter.error("empty command");
        }

        String command = tokens[0].toUpperCase();

        switch (command) {
            case "PING":
                if (tokens.length != 1) {
                    return RespWriter.error("wrong number of arguments");
                }
                return "+PONG\r\n";

            case "SET":
                if (tokens.length != 3) {
                    return RespWriter.error("wrong number of arguments");
                }
                persists(tokens, recoveryMode);
                dataStore.set(tokens[1], tokens[2]);
                return RespWriter.ok();

            case "GET":
                if (tokens.length != 2) {
                    return RespWriter.error("wrong number of arguments");
                }
                return RespWriter.bulkString(dataStore.get(tokens[1]));

            case "DEL":
                if (tokens.length < 2) {
                    return RespWriter.error("wrong number of arguments");
                }
                persists(tokens, recoveryMode);
                long count = 0;
                for (int i = 1; i < tokens.length; i++) {
                    count += dataStore.del(tokens[i]);
                }
                return RespWriter.integer(count);

            case "EXISTS":
                if (tokens.length != 2) {
                    return RespWriter.error("wrong number of arguments");
                }
                return RespWriter.integer(dataStore.exists(tokens[1]));

            case "FLUSHALL":
                if (tokens.length != 1) {
                    return RespWriter.error("wrong number of arguments");
                }
                persists(tokens, recoveryMode);
                dataStore.clear();
                return RespWriter.ok();

            case "EXPIRE":
                if(tokens.length != 3) {
                    return RespWriter.error("wrong number of arguments");
                }

                String key = tokens[1];
                long seconds;
                try {
                    seconds = Long.parseLong(tokens[2]);
                } catch (NumberFormatException e) {
                    return RespWriter.error("value is not an integer or out of range");
                }
                persists(tokens, recoveryMode);
                return RespWriter.integer(dataStore.expire(key, seconds));
            
            case "TTL":
                if(tokens.length != 2) {
                    return RespWriter.error("wrong number of arguments");
                }

                return RespWriter.integer(dataStore.ttl(tokens[1]));

            case "PERSIST":
                if(tokens.length != 2) {
                    return RespWriter.error("wrong number of arguments");
                }
                persists(tokens, recoveryMode);

                return RespWriter.integer(dataStore.persist(tokens[1]));

            default:
                return RespWriter.error("unknown command");
        }
    }
}
