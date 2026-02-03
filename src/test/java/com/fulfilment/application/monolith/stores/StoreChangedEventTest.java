package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StoreChangedEventTest {

  @Test
  public void constructorAndGetters() {
    Store s = new Store("E1");
    StoreChangedEvent ev = new StoreChangedEvent(s, StoreChangedEvent.ChangeType.CREATED);

    assertSame(s, ev.getStore());
    assertEquals(StoreChangedEvent.ChangeType.CREATED, ev.getChangeType());
  }

  @Test
  public void testEventWithUpdatedChangeType() {
    Store s = new Store("Updated Store");
    StoreChangedEvent ev = new StoreChangedEvent(s, StoreChangedEvent.ChangeType.UPDATED);

    assertEquals(StoreChangedEvent.ChangeType.UPDATED, ev.getChangeType());
    assertSame(s, ev.getStore());
  }

  @Test
  public void testEventWithNullStore() {
    StoreChangedEvent ev = new StoreChangedEvent(null, StoreChangedEvent.ChangeType.CREATED);

    assertNull(ev.getStore());
    assertEquals(StoreChangedEvent.ChangeType.CREATED, ev.getChangeType());
  }

  @Test
  public void testMultipleEventsWithDifferentStores() {
    Store s1 = new Store("Store 1");
    Store s2 = new Store("Store 2");
    
    StoreChangedEvent ev1 = new StoreChangedEvent(s1, StoreChangedEvent.ChangeType.CREATED);
    StoreChangedEvent ev2 = new StoreChangedEvent(s2, StoreChangedEvent.ChangeType.UPDATED);

    assertSame(s1, ev1.getStore());
    assertSame(s2, ev2.getStore());
    assertEquals(StoreChangedEvent.ChangeType.CREATED, ev1.getChangeType());
    assertEquals(StoreChangedEvent.ChangeType.UPDATED, ev2.getChangeType());
  }
}
