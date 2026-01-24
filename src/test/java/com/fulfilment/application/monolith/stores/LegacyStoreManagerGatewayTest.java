package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LegacyStoreManagerGatewayTest {

  @Test
  public void createWithNullStore_isNoOp() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    // should not throw
    g.createStoreOnLegacySystem(null);
  }

  @Test
  public void writeFile_and_cleanup_happyPath() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("name-with/chars?*\n");
    s.quantityProductsInStock = 42;
    // exercise the successful path which creates, writes and deletes a temp file
    g.updateStoreOnLegacySystem(s);
    // if we got here without exception, good enough for unit test
    Assertions.assertTrue(true);
  }
}
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
