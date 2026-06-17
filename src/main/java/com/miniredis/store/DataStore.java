package com.miniredis.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataStore {
    
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    
    private final ConcurrentHashMap<String, Long> expiries = new ConcurrentHashMap<>();
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    public DataStore() {
        scheduler.scheduleAtFixedRate(
            this::cleanupExpiredKeys,
            100,
            100,
            TimeUnit.MILLISECONDS
        );
    }

    public void set(String key, String value) {
        store.put(key, value);
        expiries.remove(key); // Clear TTL on overwrite
    }

    public int expire(String key, long seconds) {
        if (isExpired(key)) {
            deleteInternal(key);
            return 0;
        }
        if(!store.containsKey(key)) {
            return 0;
        }

        long expiryTime = System.currentTimeMillis() + (seconds * 1000L);

        expiries.put(key, expiryTime);
        
        return 1;
    }

    public void setWithExpiry(String key, String value, long ttlMs) {
        store.put(key, value);
        expiries.put(key, System.currentTimeMillis() + ttlMs);
    }

    public String get(String key) {
        if (isExpired(key)) {
            deleteInternal(key);
            return null;
        }
        return store.get(key);
    }

    public long del(String key) {
        if (isExpired(key)) {
            deleteInternal(key);
            return 0;
        }
        expiries.remove(key);
        return store.remove(key) != null ? 1 : 0;
    }

    public long exists(String key) {
        if (isExpired(key)) {
            deleteInternal(key);
            return 0;
        }
        return store.containsKey(key) ? 1 : 0;
    }

    public void clear() {
        store.clear();
        expiries.clear();
    }

    private boolean isExpired(String key) {
        Long expiry = expiries.get(key);
        if (expiry == null) {
            return false;
        }
        return System.currentTimeMillis() > expiry;
    }

    private void deleteInternal(String key) {
        store.remove(key);
        expiries.remove(key);
    }

    public long ttl(String key) {
        //-2: Key does not exist
        //-1: Key exists but with no TTL
        //>0: Remaining seconds
        if(isExpired(key)) {
            deleteInternal(key);
            return -2;
        }

        if(!store.containsKey(key)) {
            return -2;
        }

        Long expiryTime = expiries.get(key);

        if(expiryTime == null) {
            return -1;
        }

        long remainingMs = expiryTime - System.currentTimeMillis();
        long remaining = (remainingMs + 999L) / 1000L;

        return Math.max(remaining, 0);
    }

    public int persist(String key) {
        if (isExpired(key)) {
            deleteInternal(key);
            return 0;
        }
        if(!store.containsKey(key)) {
            return 0;
        }

        return expiries.remove(key) != null
        ? 1
        : 0;
    }

    public void cleanupExpiredKeys() {

        long now = System.currentTimeMillis();

        for(Map.Entry<String, Long> entry: expiries.entrySet()) {

            String key = entry.getKey();
            Long expiryTime = entry.getValue();

            if(expiryTime <= now) {

                System.out.println("Expiring key : " + key);
                store.remove(key);
                expiries.remove(key);
            }
        }
    }
    public void shutdown() {
        scheduler.shutdown();
    }

}