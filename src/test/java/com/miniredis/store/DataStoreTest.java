package com.miniredis.store;

import com.miniredis.Config;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class DataStoreTest {

    private DataStore dataStore;

    @Before
    public void setUp() {
        dataStore = new DataStore();
    }

    @After
    public void tearDown() {
        dataStore.shutdown();
    }

    @Test
    public void testLruEvictionOnInsert() {
        // Since Config.MAX_KEYS is 5
        dataStore.set("k1", "v1");
        dataStore.set("k2", "v2");
        dataStore.set("k3", "v3");
        dataStore.set("k4", "v4");
        dataStore.set("k5", "v5");

        // Access k1 (updates its access order to make it recently used)
        assertNotNull(dataStore.get("k1"));

        // Insert 6th key, should evict the eldest (which is now k2 since k1 was accessed)
        dataStore.set("k6", "v6");

        // k2 should be evicted, k1 should still exist!
        assertNull(dataStore.get("k2"));
        assertNotNull(dataStore.get("k1"));
        assertNotNull(dataStore.get("k3"));
        assertNotNull(dataStore.get("k4"));
        assertNotNull(dataStore.get("k5"));
        assertNotNull(dataStore.get("k6"));
    }

    @Test
    public void testLruEvictionWithoutAccess() {
        dataStore.set("k1", "v1");
        dataStore.set("k2", "v2");
        dataStore.set("k3", "v3");
        dataStore.set("k4", "v4");
        dataStore.set("k5", "v5");

        // Add 6th key without accessing anything beforehand
        dataStore.set("k6", "v6");

        // k1 (the eldest) should be evicted
        assertNull(dataStore.get("k1"));
        assertNotNull(dataStore.get("k2"));
        assertNotNull(dataStore.get("k3"));
        assertNotNull(dataStore.get("k4"));
        assertNotNull(dataStore.get("k5"));
        assertNotNull(dataStore.get("k6"));
    }

    @Test
    public void testLruEvictionOnSetUpdatesAccessOrder() {
        dataStore.set("k1", "v1");
        dataStore.set("k2", "v2");
        dataStore.set("k3", "v3");
        dataStore.set("k4", "v4");
        dataStore.set("k5", "v5");

        // Overwrite k1 to update its access order
        dataStore.set("k1", "v1_updated");

        // Insert 6th key
        dataStore.set("k6", "v6");

        // k2 (new eldest) should be evicted, k1 must remain
        assertNull(dataStore.get("k2"));
        assertNotNull(dataStore.get("k1"));
        assertEquals("v1_updated", dataStore.get("k1"));
    }

    @Test
    public void testEvictionRemovesExpiries() {
        dataStore.setWithExpiry("k1", "v1", 100000); // Expiry far in future
        dataStore.set("k2", "v2");
        dataStore.set("k3", "v3");
        dataStore.set("k4", "v4");
        dataStore.set("k5", "v5");

        // Verify expiry is set (returns remaining seconds, which should be >0)
        assertTrue(dataStore.ttl("k1") > 0);

        // Insert 6th key, which evicts k1
        dataStore.set("k6", "v6");

        // k1 should be gone from store and its expiry from expiries map
        assertNull(dataStore.get("k1"));
        assertEquals(-2, dataStore.ttl("k1")); // -2 means key does not exist
    }

    @Test
    public void testEvictionPriorToInsertion() {
        // Fill store to Config.MAX_KEYS
        dataStore.set("k1", "v1");
        dataStore.set("k2", "v2");
        dataStore.set("k3", "v3");
        dataStore.set("k4", "v4");
        dataStore.set("k5", "v5");

        assertEquals(Config.MAX_KEYS, dataStore.size());

        // Overwriting an existing key should NOT trigger eviction
        dataStore.set("k5", "v5_new");
        assertEquals(Config.MAX_KEYS, dataStore.size());
        assertNotNull(dataStore.get("k1")); // k1 should still exist

        // Inserting a brand new key "k6" triggers eviction.
        // Since we accessed k1 at line 120, k2 is now the eldest key and should be evicted.
        dataStore.set("k6", "v6");
        assertEquals(Config.MAX_KEYS, dataStore.size());
        assertNull(dataStore.get("k2")); // k2 (eldest) must be evicted
        assertNotNull(dataStore.get("k1")); // k1 should still exist
    }
}
