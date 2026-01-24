package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceIntegrationTest2 {

  @Inject
  StoreResource resource;

  @Test
  @Transactional
  public void update_happyPath_updatesFields() {
    Store s = new Store("integration-store");
    s.persist();

    Store upd = new Store("integration-updated");
    upd.quantityProductsInStock = 99;

    Store result = resource.update(s.id, upd);
    Assertions.assertNotNull(result);
    Assertions.assertEquals("integration-updated", result.name);
    Assertions.assertEquals(99, result.quantityProductsInStock);
  }

  @Test
  @Transactional
  public void delete_happyPath_triggersDeleteAndFlush() {
    Store s = new Store("to-delete");
    s.persist();

    resource.delete(s.id);

    Store maybe = Store.findById(s.id);
    Assertions.assertNull(maybe);
  }
}
