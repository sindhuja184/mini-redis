package com.miniredis.command;

import com.miniredis.protocol.RespWriter;
import com.miniredis.store.DataStore;

public class CommandHandler {

    private final DataStore dataStore;

    public CommandHandler(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public String handle(String[] tokens) {
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
                dataStore.clear();
                return RespWriter.ok();

            default:
                return RespWriter.error("unknown command");
        }
    }
}
