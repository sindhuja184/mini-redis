package com.miniredis.command;

import com.miniredis.store.DataStore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommandHandlerTest {

    private DataStore dataStore;
    private CommandHandler handler;

    @Before
    public void setUp() {
        dataStore = new DataStore();
        handler = new CommandHandler(dataStore);
    }

    @Test
    public void testPing() {
        assertEquals("+PONG\r\n", handler.handle(new String[]{"PING"}));
        assertEquals("+PONG\r\n", handler.handle(new String[]{"ping"}));
        assertEquals("+PONG\r\n", handler.handle(new String[]{"pInG"}));
        
        // Wrong number of arguments
        assertTrue(handler.handle(new String[]{"PING", "extra"}).startsWith("-ERR wrong number of arguments"));
    }

    @Test
    public void testSetAndGet() {
        // Set key
        assertEquals("+OK\r\n", handler.handle(new String[]{"SET", "mykey", "myvalue"}));
        
        // Get key
        assertEquals("$7\r\nmyvalue\r\n", handler.handle(new String[]{"GET", "mykey"}));
        
        // Get non-existent
        assertEquals("$-1\r\n", handler.handle(new String[]{"GET", "nonexistent"}));

        // Set case-insensitive command
        assertEquals("+OK\r\n", handler.handle(new String[]{"set", "anotherkey", "val"}));
        assertEquals("$3\r\nval\r\n", handler.handle(new String[]{"get", "anotherkey"}));

        // Wrong number of arguments
        assertTrue(handler.handle(new String[]{"SET", "key"}).startsWith("-ERR wrong number of arguments"));
        assertTrue(handler.handle(new String[]{"SET"}).startsWith("-ERR wrong number of arguments"));
        assertTrue(handler.handle(new String[]{"GET"}).startsWith("-ERR wrong number of arguments"));
        assertTrue(handler.handle(new String[]{"GET", "k", "extra"}).startsWith("-ERR wrong number of arguments"));
    }

    @Test
    public void testDel() {
        handler.handle(new String[]{"SET", "key1", "val1"});
        handler.handle(new String[]{"SET", "key2", "val2"});

        // Delete single key
        assertEquals(":1\r\n", handler.handle(new String[]{"DEL", "key1"}));
        assertEquals("$-1\r\n", handler.handle(new String[]{"GET", "key1"}));

        // Delete multiple keys (one exists, one doesn't)
        assertEquals(":1\r\n", handler.handle(new String[]{"DEL", "key2", "key3"}));

        // Delete non-existent key
        assertEquals(":0\r\n", handler.handle(new String[]{"DEL", "key1"}));

        // Wrong number of arguments
        assertTrue(handler.handle(new String[]{"DEL"}).startsWith("-ERR wrong number of arguments"));
    }

    @Test
    public void testExists() {
        handler.handle(new String[]{"SET", "exkey", "val"});

        assertEquals(":1\r\n", handler.handle(new String[]{"EXISTS", "exkey"}));
        assertEquals(":0\r\n", handler.handle(new String[]{"EXISTS", "missingkey"}));

        // Wrong number of arguments
        assertTrue(handler.handle(new String[]{"EXISTS"}).startsWith("-ERR wrong number of arguments"));
        assertTrue(handler.handle(new String[]{"EXISTS", "a", "b"}).startsWith("-ERR wrong number of arguments"));
    }

    @Test
    public void testFlushAll() {
        handler.handle(new String[]{"SET", "k1", "v1"});
        handler.handle(new String[]{"SET", "k2", "v2"});

        assertEquals("+OK\r\n", handler.handle(new String[]{"FLUSHALL"}));

        assertEquals("$-1\r\n", handler.handle(new String[]{"GET", "k1"}));
        assertEquals("$-1\r\n", handler.handle(new String[]{"GET", "k2"}));

        // Wrong number of arguments
        assertTrue(handler.handle(new String[]{"FLUSHALL", "extra"}).startsWith("-ERR wrong number of arguments"));
    }

    @Test
    public void testUnknownCommand() {
        assertTrue(handler.handle(new String[]{"INVALID", "arg"}).startsWith("-ERR unknown command"));
        assertTrue(handler.handle(new String[]{"what"}).startsWith("-ERR unknown command"));
    }

    @Test
    public void testTtlExpiration() throws InterruptedException {
        // We set expiry of 100ms
        dataStore.setWithExpiry("tempkey", "tempval", 100);

        // Immediately check exists and get
        assertEquals(":1\r\n", handler.handle(new String[]{"EXISTS", "tempkey"}));
        assertEquals("$7\r\ntempval\r\n", handler.handle(new String[]{"GET", "tempkey"}));

        // Sleep to let it expire
        Thread.sleep(150);

        // Check exists and get again
        assertEquals(":0\r\n", handler.handle(new String[]{"EXISTS", "tempkey"}));
        assertEquals("$-1\r\n", handler.handle(new String[]{"GET", "tempkey"}));
    }

    @Test
    public void testExpireTtlAndPersistCommands() throws InterruptedException {
        // Test basic setting and TTL check
        assertEquals("+OK\r\n", handler.handle(new String[]{"SET", "ttlkey", "ttlval"}));
        assertEquals(":-1\r\n", handler.handle(new String[]{"TTL", "ttlkey"})); // No expiry initially

        // Set expire
        assertEquals(":1\r\n", handler.handle(new String[]{"EXPIRE", "ttlkey", "2"}));
        
        // TTL should be around 2 seconds
        String ttlResp = handler.handle(new String[]{"TTL", "ttlkey"});
        assertTrue(ttlResp.equals(":2\r\n") || ttlResp.equals(":1\r\n"));

        // Persist
        assertEquals(":1\r\n", handler.handle(new String[]{"PERSIST", "ttlkey"}));
        assertEquals(":-1\r\n", handler.handle(new String[]{"TTL", "ttlkey"}));

        // Persist again (now it has no TTL)
        assertEquals(":0\r\n", handler.handle(new String[]{"PERSIST", "ttlkey"}));

        // Test non-existent keys
        assertEquals(":0\r\n", handler.handle(new String[]{"EXPIRE", "nonexistent", "10"}));
        assertEquals(":-2\r\n", handler.handle(new String[]{"TTL", "nonexistent"}));
        assertEquals(":0\r\n", handler.handle(new String[]{"PERSIST", "nonexistent"}));

        // Invalid EXPIRE arguments
        assertTrue(handler.handle(new String[]{"EXPIRE", "ttlkey", "abc"}).contains("value is not an integer or out of range"));
        assertTrue(handler.handle(new String[]{"EXPIRE"}).contains("wrong number of arguments"));
        assertTrue(handler.handle(new String[]{"EXPIRE", "ttlkey"}).contains("wrong number of arguments"));
        assertTrue(handler.handle(new String[]{"TTL"}).contains("wrong number of arguments"));
        assertTrue(handler.handle(new String[]{"PERSIST"}).contains("wrong number of arguments"));

        // Check expiration behavior
        assertEquals(":1\r\n", handler.handle(new String[]{"EXPIRE", "ttlkey", "1"}));
        Thread.sleep(1100);

        // Key is expired now
        assertEquals("$-1\r\n", handler.handle(new String[]{"GET", "ttlkey"}));
        assertEquals(":0\r\n", handler.handle(new String[]{"EXISTS", "ttlkey"}));
        assertEquals(":-2\r\n", handler.handle(new String[]{"TTL", "ttlkey"}));
        assertEquals(":0\r\n", handler.handle(new String[]{"PERSIST", "ttlkey"}));
        assertEquals(":0\r\n", handler.handle(new String[]{"EXPIRE", "ttlkey", "5"}));
        assertEquals(":0\r\n", handler.handle(new String[]{"DEL", "ttlkey"}));
    }
}
