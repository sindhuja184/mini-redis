package com.miniredis.manager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;

import com.miniredis.protocol.RespWriter;

public class AofManager {
    private final BufferedWriter writer;

    public AofManager(String filePath) throws IOException {
        this.writer = new BufferedWriter(
            new FileWriter(filePath, true)
        );
    }

    public synchronized void append(String []tokens) throws IOException {
        writer.write(RespWriter.command(tokens));
        writer.flush();
    }

    public void close() throws IOException {
        writer.close();
    }


}
