package com.miniredis.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.miniredis.command.CommandHandler;
import com.miniredis.protocol.RespParser;

public class AofRecoveryManager {
    private final CommandHandler commandHandler;

    public AofRecoveryManager(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

     public void recover(String path)
            throws IOException {

        File file = new File(path);

        if (!file.exists()) {
            return;
        }

        try (FileInputStream fis =
                     new FileInputStream(file)) {

            while (true) {

                String[] tokens =
                        RespParser.parse(fis);

                if (tokens == null) {
                    break;
                }

                commandHandler.handle(
                        tokens,
                        true
                );
            }
        }
    }
}
