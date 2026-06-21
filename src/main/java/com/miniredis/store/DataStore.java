package com.miniredis.store;

import com.miniredis.Config;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataStore {
    
    private final ConcurrentHashMap<String, Long> expiries = new ConcurrentHashMap<>();

    private final Map<String, String> store = Collections.synchronizedMap(
        new LinkedHashMap<String, String>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                if (size() > Config.MAX_KEYS) {
                    expiries.remove(eldest.getKey());
                    return true;
                }
                return false;
            }
        }
    );

    public Map<String, String> getStoreSnapshot() {
        synchronized (store) {
            return new LinkedHashMap<>(store);
        }
    }

    public Map<String, Long> getExpirySnapshot() {
        synchronized (store) {
            return new ConcurrentHashMap<>(expiries);
        }
    }

    public void restore(
        Map<String, String> storeData,
        Map<String, Long> expiryData
    ) {
        synchronized (store) {
            store.clear();
            expiries.clear();

            store.putAll(storeData);
            expiries.putAll(expiryData);
        }
    }
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
        synchronized (store) {
            store.put(key, value);
            expiries.remove(key); // Clear TTL on overwrite
        }
    }

    public int expire(String key, long seconds) {
        synchronized (store) {
            if (isExpired(key)) {
                deleteInternal(key);
                return 0;
            }
            if (!store.containsKey(key)) {
                return 0;
            }

            long expiryTime = System.currentTimeMillis() + (seconds * 1000L);
            expiries.put(key, expiryTime);
            return 1;
        }
    }

    public void setWithExpiry(String key, String value, long ttlMs) {
        synchronized (store) {
            store.put(key, value);
            expiries.put(key, System.currentTimeMillis() + ttlMs);
        }
    }

    public String get(String key) {
        synchronized (store) {
            if (isExpired(key)) {
                deleteInternal(key);
                return null;
            }
            return store.get(key);
        }
    }

    public long del(String key) {
        synchronized (store) {
            if (isExpired(key)) {
                deleteInternal(key);
                return 0;
            }
            expiries.remove(key);
            return store.remove(key) != null ? 1 : 0;
        }
    }

    public long exists(String key) {
        synchronized (store) {
            if (isExpired(key)) {
                deleteInternal(key);
                return 0;
            }
            return store.containsKey(key) ? 1 : 0;
        }
    }

    public void clear() {
        synchronized (store) {
            store.clear();
            expiries.clear();
        }
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
        synchronized (store) {
            if (isExpired(key)) {
                deleteInternal(key);
                return -2;
            }

            if (!store.containsKey(key)) {
                return -2;
            }

            Long expiryTime = expiries.get(key);

            if (expiryTime == null) {
                return -1;
            }

            long remainingMs = expiryTime - System.currentTimeMillis();
            long remaining = (remainingMs + 999L) / 1000L;

            return Math.max(remaining, 0);
        }
    }

    public int persist(String key) {
        synchronized (store) {
            if (isExpired(key)) {
                deleteInternal(key);
                return 0;
            }
            if (!store.containsKey(key)) {
                return 0;
            }

            return expiries.remove(key) != null ? 1 : 0;
        }
    }

    public void cleanupExpiredKeys() {
        long now = System.currentTimeMillis();

        for (Map.Entry<String, Long> entry : expiries.entrySet()) {
            String key = entry.getKey();
            Long expiryTime = entry.getValue();

            if (expiryTime <= now) {
                synchronized (store) {
                    Long currentExpiry = expiries.get(key);
                    if (currentExpiry != null && currentExpiry.equals(expiryTime) && currentExpiry <= now) {
                        store.remove(key);
                        expiries.remove(key);
                    }
                }
            }
        }
    }

    public int size() {
        synchronized (store) {
            return store.size();
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}