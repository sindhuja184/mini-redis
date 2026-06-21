package com.miniredis.protocol;

public class RespWriter {

    public static String ok() {
        return "+OK\r\n";
    }

    public static String error(String message) {
        return "-ERR " + message + "\r\n";
    }

    public static String integer(long value) {
        return ":" + value + "\r\n";
    }

    public static String bulkString(String value) {

        if (value == null) {
            return nullBulk();
        }

        return "$"
                + value.length()
                + "\r\n"
                + value
                + "\r\n";
    }

    public static String nullBulk() {
        return "$-1\r\n";
    }
    public static String command(String []tokens) {
        StringBuilder sb = new StringBuilder();

        sb.append("*")
        .append(tokens.length)
        .append("\r\n");

        for(String token : tokens) {
            sb.append("$")
            .append(token.length())
            .append("\r\n")
            .append(token)
            .append("\r\n");
        }
        return sb.toString();
    }
}