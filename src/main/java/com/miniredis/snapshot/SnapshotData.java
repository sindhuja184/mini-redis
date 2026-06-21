package com.miniredis.snapshot;

import java.io.Serializable;
import java.util.Map;

public class SnapshotData implements Serializable{
    
    private static final long serialVersionUID = 1L;

    private final Map<String, String> store;
    private final Map<String, Long> expiries;

    public SnapshotData(Map<String, String> store, Map<String, Long> expiries) {
        this.store = store;
        this.expiries = expiries;
    }

    public Map<String, String> getStore() {
        return store;
    }

    public Map<String, Long> getExpiries() {
        return expiries;
    }
}
