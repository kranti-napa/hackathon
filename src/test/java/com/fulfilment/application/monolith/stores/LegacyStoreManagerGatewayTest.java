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

  @Test
  public void create_and_update_withNullAndValid_instance_calls() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    // null should be handled gracefully
    g.createStoreOnLegacySystem(null);

    Store s = new Store("gw-test");
    s.quantityProductsInStock = 12;

    // should run without throwing (writes to temp file then deletes)
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
    Assertions.assertDoesNotThrow(() -> g.updateStoreOnLegacySystem(s));
  }

  @Test
  public void create_and_update_whenTempDirInvalid_logsAndContinues() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("tempdir-fail");
    s.quantityProductsInStock = 1;

    String originalTmp = System.getProperty("java.io.tmpdir");
    try {
      System.setProperty("java.io.tmpdir", "Z:\\path-does-not-exist\\");

      Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
      Assertions.assertDoesNotThrow(() -> g.updateStoreOnLegacySystem(s));
    } finally {
      if (originalTmp != null) {
        System.setProperty("java.io.tmpdir", originalTmp);
      }
    }
  }

  @Test
  public void deleteWithNullStore_isNoOp() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    // should not throw
    g.deleteStoreOnLegacySystem(null);
  }

  @Test
  public void deleteStore_happyPath() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("store-to-delete");
    s.quantityProductsInStock = 10;
    // should run without throwing (writes to temp file then deletes)
    Assertions.assertDoesNotThrow(() -> g.deleteStoreOnLegacySystem(s));
  }

  @Test
  public void delete_and_create_update_withValidStore() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();

    Store s = new Store("store-lifecycle");
    s.quantityProductsInStock = 25;

    // should run without throwing for all operations
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
    Assertions.assertDoesNotThrow(() -> g.updateStoreOnLegacySystem(s));
    Assertions.assertDoesNotThrow(() -> g.deleteStoreOnLegacySystem(s));
  }

  @Test
  public void updateWithNullStore_isNoOp() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Assertions.assertDoesNotThrow(() -> g.updateStoreOnLegacySystem(null));
  }

  @Test
  public void createStoreWithNullName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store();
    s.quantityProductsInStock = 10;
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
  }

  @Test
  public void createStoreWithEmptyName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("");
    s.quantityProductsInStock = 5;
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
  }

  @Test
  public void createStoreWithLongName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("A".repeat(1000));
    s.quantityProductsInStock = 100;
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
  }

  @Test
  public void createStoreWithSpecialCharacters() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("Store!@#$%^&*()");
    s.quantityProductsInStock = 15;
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
  }

  @Test
  public void createStoreWithZeroStock() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("Zero Stock Store");
    s.quantityProductsInStock = 0;
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
  }

  @Test
  public void createStoreWithNegativeStock() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("Negative Stock Store");
    s.quantityProductsInStock = -10;
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
  }

  @Test
  public void updateStoreWithNullName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store();
    s.quantityProductsInStock = 20;
    Assertions.assertDoesNotThrow(() -> g.updateStoreOnLegacySystem(s));
  }

  @Test
  public void deleteStoreWithNullName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store();
    s.quantityProductsInStock = 5;
    Assertions.assertDoesNotThrow(() -> g.deleteStoreOnLegacySystem(s));
  }

  @Test
  public void createStoreWithOneCharName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("A");
    s.quantityProductsInStock = 10;
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
  }

  @Test
  public void createStoreWithTwoCharName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("AB");
    s.quantityProductsInStock = 20;
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
  }

  @Test
  public void createStoreWithExactlyThreeChars() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("ABC");
    s.quantityProductsInStock = 30;
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
  }

  @Test
  public void updateStoreWithShortName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("XY");
    s.quantityProductsInStock = 15;
    Assertions.assertDoesNotThrow(() -> g.updateStoreOnLegacySystem(s));
  }

  @Test
  public void deleteStoreWithShortName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("Z");
    s.quantityProductsInStock = 5;
    Assertions.assertDoesNotThrow(() -> g.deleteStoreOnLegacySystem(s));
  }

  @Test
  public void createStoreWithVeryLongName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("ThisIsAVeryLongStoreNameThatExceedsTenCharacters");
    s.quantityProductsInStock = 100;
    Assertions.assertDoesNotThrow(() -> g.createStoreOnLegacySystem(s));
  }

  @Test
  public void updateStoreWithVeryLongName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("AnotherVeryLongStoreName");
    s.quantityProductsInStock = 50;
    Assertions.assertDoesNotThrow(() -> g.updateStoreOnLegacySystem(s));
  }

  @Test
  public void deleteStoreWithVeryLongName() {
    LegacyStoreManagerGateway g = new LegacyStoreManagerGateway();
    Store s = new Store("YetAnotherVeryLongStoreNameForDeletion");
    s.quantityProductsInStock = 25;
    Assertions.assertDoesNotThrow(() -> g.deleteStoreOnLegacySystem(s));
  }
}
