package com.fulfilment.application.monolith.stores;

public class StoreChangedEvent {

    private final Store store;
    private final ChangeType changeType;

    public enum ChangeType {
        CREATED,
        UPDATED
    }

    public StoreChangedEvent(Store store, ChangeType changeType) {
        this.store = store;
        this.changeType = changeType;
    }

    public Store getStore() {
        return store;
    }

    public ChangeType getChangeType() {
        return changeType;
    }
}
