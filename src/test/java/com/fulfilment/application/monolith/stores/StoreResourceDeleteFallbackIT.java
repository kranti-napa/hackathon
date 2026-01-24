package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceDeleteFallbackIT {

  @Inject
  StoreResource resource;

  @Test
  @Transactional
  public void delete_whenInstanceDeleteNoop_triggersDeleteByIdFallback() {
    // create an anonymous subclass that overrides instance delete() to simulate a no-op
    Store s = new Store("to-delete-fallback") {
      @Override
      public void delete() {
        // simulate a delete that did nothing (e.g., persistence layer ignored instance delete)
      }
    };
    s.persist();

    // call resource.delete which should attempt instance.delete(), then detect still-present
    // entity and call static deleteById(id) as a defensive fallback
    resource.delete(s.id);

    Store maybe = Store.findById(s.id);
    Assertions.assertNull(maybe, "Store should be removed by defensive deleteById fallback");
  }
}
