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
}
