package com.miniredis.protocol;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;

public class RespParserTest {

    @Test
    public void testParseValidArray() throws IOException {
        String data = "*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n";
        InputStream in = new ByteArrayInputStream(data.getBytes());
        String[] tokens = RespParser.parse(in);
        
        assertNotNull(tokens);
        assertEquals(2, tokens.length);
        assertEquals("GET", tokens[0]);
        assertEquals("key", tokens[1]);
    }

    @Test
    public void testParseEmptyInput() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        String[] tokens = RespParser.parse(in);
        assertNull(tokens);
    }

    @Test
    public void testParseInvalidArrayPrefix() {
        String data = "+OK\r\n";
        InputStream in = new ByteArrayInputStream(data.getBytes());
        try {
            RespParser.parse(in);
            fail("Expected IOException due to missing * prefix");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Expected RESP Array"));
        }
    }

    @Test
    public void testParseInvalidBulkStringHeader() {
        String data = "*1\r\n+INVALID\r\n";
        InputStream in = new ByteArrayInputStream(data.getBytes());
        try {
            RespParser.parse(in);
            fail("Expected IOException due to missing $ prefix for bulk string");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Expected Bulk String"));
        }
    }

    @Test
    public void testParseMultipleTokens() throws IOException {
        String data = "*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$5\r\nalice\r\n";
        InputStream in = new ByteArrayInputStream(data.getBytes());
        String[] tokens = RespParser.parse(in);
        
        assertNotNull(tokens);
        assertEquals(3, tokens.length);
        assertEquals("SET", tokens[0]);
        assertEquals("name", tokens[1]);
        assertEquals("alice", tokens[2]);
    }

    @Test
    public void testParseSingleToken() throws IOException {
        String data = "*1\r\n$4\r\nPING\r\n";
        InputStream in = new ByteArrayInputStream(data.getBytes());
        String[] tokens = RespParser.parse(in);
        
        assertNotNull(tokens);
        assertEquals(1, tokens.length);
        assertEquals("PING", tokens[0]);
    }
}
