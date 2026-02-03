package com.fulfilment.application.monolith.warehouses.domain.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WarehouseTest {

  @Test
  public void testDefaultConstructor() {
    Warehouse warehouse = new Warehouse();
    assertNull(warehouse.businessUnitCode);
    assertNull(warehouse.location);
    assertNull(warehouse.capacity);
    assertNull(warehouse.stock);
  }

  @Test
  public void testFieldAssignments() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU-001";
    warehouse.location = "New York";
    warehouse.capacity = 1000;
    warehouse.stock = 500;

    assertEquals("BU-001", warehouse.businessUnitCode);
    assertEquals("New York", warehouse.location);
    assertEquals(1000, warehouse.capacity);
    assertEquals(500, warehouse.stock);
  }

  @Test
  public void testZeroValues() {
    Warehouse warehouse = new Warehouse();
    warehouse.capacity = 0;
    warehouse.stock = 0;

    assertEquals(0, warehouse.capacity);
    assertEquals(0, warehouse.stock);
  }

  @Test
  public void testNullValues() {
    Warehouse warehouse = new Warehouse();
    assertNull(warehouse.businessUnitCode);
    assertNull(warehouse.location);
    assertNull(warehouse.capacity);
    assertNull(warehouse.stock);
    assertNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
  }

  @Test
  public void testNegativeStock() {
    Warehouse warehouse = new Warehouse();
    warehouse.stock = -10;
    assertEquals(-10, warehouse.stock);
  }
}
