package com.miniredis.manager;

import java.io.*;

import com.miniredis.snapshot.SnapshotData;
import com.miniredis.store.DataStore;

public class RdbManager {
    
    private final String filePath;

    public RdbManager(String filePath) {
        this.filePath = filePath;
    }

    public void save(DataStore dataStore) throws IOException {

        SnapshotData snapshot =
                new SnapshotData(
                        dataStore.getStoreSnapshot(),
                        dataStore.getExpirySnapshot()
                );

        try(ObjectOutputStream out =
                    new ObjectOutputStream(
                            new FileOutputStream(filePath))) {

            out.writeObject(snapshot);
        }
    }
    public void load(DataStore dataStore) throws IOException {
        File file = new File(filePath);

        if(!file.exists()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            SnapshotData snapshot = (SnapshotData) in.readObject();

            dataStore.restore(snapshot.getStore(), snapshot.getExpiries());
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found during snapshot deserialization", e);
        }
    }
}
