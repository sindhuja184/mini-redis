package com.miniredis.protocol;

import java.io.IOException;
import java.io.InputStream;

public class RespParser {

    public static String[] parse(InputStream inputStream) throws IOException {
        String firstLine = readLine(inputStream);

        if (firstLine == null) {
            return null;
        }

        if (!firstLine.startsWith("*")) {
            throw new IOException("Expected RESP Array");
        }

        int count = Integer.parseInt(firstLine.substring(1));

        String[] tokens = new String[count];

        for (int i = 0; i < count; i++) {
            String bulkHeader = readLine(inputStream);

            if (bulkHeader == null || !bulkHeader.startsWith("$")) {
                throw new IOException("Expected Bulk String");
            }

            tokens[i] = readLine(inputStream);
        }

        return tokens;
    }

    private static String readLine(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = inputStream.read()) != -1) {
            if (b == '\r') {
                int next = inputStream.read();
                if (next == '\n') {
                    return sb.toString();
                }
                sb.append('\r');
                if (next != -1) {
                    sb.append((char) next);
                }
            } else {
                sb.append((char) b);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}