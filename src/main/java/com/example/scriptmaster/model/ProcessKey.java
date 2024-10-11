package com.example.scriptmaster.model;

import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

@Data
public class ProcessKey {
    private final String scriptUUID;
    private final long uniqueId;

    @PersistenceCreator
    public ProcessKey(String scriptUUID, long uniqueId) {
        this.scriptUUID = scriptUUID;
        this.uniqueId = uniqueId;
    }

    public ProcessKey(String scriptUUID) {
        this.scriptUUID = scriptUUID;
        this.uniqueId = System.nanoTime();
    }

    @Override
    public String toString() {
        return scriptUUID + "-" + uniqueId;
    }
}
