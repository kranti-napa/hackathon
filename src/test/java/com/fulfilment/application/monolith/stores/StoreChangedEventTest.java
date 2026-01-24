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
}
