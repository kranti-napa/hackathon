package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StoreTest {

  @Test
  public void testDefaultConstructor() {
    Store store = new Store();
    assertNull(store.name);
    assertEquals(0, store.quantityProductsInStock); // primitive int defaults to 0
    assertNull(store.id);
  }

  @Test
  public void testConstructorWithName() {
    Store store = new Store("Test Store");
    assertEquals("Test Store", store.name);
    assertEquals(0, store.quantityProductsInStock); // primitive int defaults to 0
    assertNull(store.id);
  }

  @Test
  public void testFieldAssignments() {
    Store store = new Store("My Store");
    store.quantityProductsInStock = 100;
    
    assertEquals("My Store", store.name);
    assertEquals(100, store.quantityProductsInStock);
  }

  @Test
  public void testNullName() {
    Store store = new Store(null);
    assertNull(store.name);
  }

  @Test
  public void testEmptyName() {
    Store store = new Store("");
    assertEquals("", store.name);
  }

  @Test
  public void testZeroStock() {
    Store store = new Store("Zero Stock");
    store.quantityProductsInStock = 0;
    assertEquals(0, store.quantityProductsInStock);
  }

  @Test
  public void testNegativeStock() {
    Store store = new Store("Negative Stock");
    store.quantityProductsInStock = -5;
    assertEquals(-5, store.quantityProductsInStock);
  }

  @Test
  public void testLongName() {
    String longName = "A".repeat(500);
    Store store = new Store(longName);
    assertEquals(longName, store.name);
  }

  @Test
  public void testStoreIdAssignment() {
    Store store = new Store("ID Test");
    assertNull(store.id);
    store.id = 999L;
    assertEquals(999L, store.id);
  }

  @Test
  public void testMaxStockValue() {
    Store store = new Store("Max Stock");
    store.quantityProductsInStock = Integer.MAX_VALUE;
    assertEquals(Integer.MAX_VALUE, store.quantityProductsInStock);
  }
}
