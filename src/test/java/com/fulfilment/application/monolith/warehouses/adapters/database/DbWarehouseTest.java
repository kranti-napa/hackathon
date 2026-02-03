package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class DbWarehouseTest {

  @Test
  public void testToWarehouseMapsFields() {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = "BU1";
    db.location = "LOC1";
    db.capacity = 100;
    db.stock = 10;
    db.createdAt = LocalDateTime.of(2020, 1, 2, 3, 4);
    db.archivedAt = LocalDateTime.of(2021, 2, 3, 4, 5);

    Warehouse w = db.toWarehouse();
    assertEquals("BU1", w.businessUnitCode);
    assertEquals("LOC1", w.location);
    assertEquals(100, w.capacity);
    assertEquals(10, w.stock);
    assertEquals(db.createdAt, w.createdAt);
    assertEquals(db.archivedAt, w.archivedAt);
  }

  @Test
  public void testToWarehousePreservesAllFields() {
    DbWarehouse db = new DbWarehouse();
    db.id = 123L;
    db.businessUnitCode = "TEST-CODE";
    db.location = "Test Location";
    db.capacity = 500;
    db.stock = 250;
    db.createdAt = LocalDateTime.of(2023, 5, 15, 10, 30);
    db.archivedAt = LocalDateTime.of(2024, 1, 1, 0, 0);

    Warehouse w = db.toWarehouse();
    assertEquals("TEST-CODE", w.businessUnitCode);
    assertEquals("Test Location", w.location);
    assertEquals(500, w.capacity);
    assertEquals(250, w.stock);
    assertEquals(db.createdAt, w.createdAt);
    assertEquals(db.archivedAt, w.archivedAt);
  }

  @Test
  public void testToWarehouseWithZeroValues() {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = "ZERO";
    db.capacity = 0;
    db.stock = 0;

    Warehouse w = db.toWarehouse();
    assertEquals(0, w.capacity);
    assertEquals(0, w.stock);
  }
}
