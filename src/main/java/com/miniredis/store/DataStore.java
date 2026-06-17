package com.miniredis.store;

import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> expiries = new ConcurrentHashMap<>();

    public void set(String key, String value) {
        store.put(key, value);
        expiries.remove(key); // Clear TTL on overwrite
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
}