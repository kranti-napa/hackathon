package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LegacyStoreManagerGatewayTest {

  @Inject
  LegacyStoreManagerGateway gateway;

  @Test
  public void create_and_update_withNullAndValid() {
    // null should be handled gracefully
    gateway.createStoreOnLegacySystem(null);

    Store s = new Store("gw-test");
    s.quantityProductsInStock = 12;

    // should run without throwing (writes to temp file then deletes)
    Assertions.assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(s));
    Assertions.assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(s));
  }
}
