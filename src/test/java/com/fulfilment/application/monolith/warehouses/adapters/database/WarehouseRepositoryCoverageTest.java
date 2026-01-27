package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;

@QuarkusTest
public class WarehouseRepositoryCoverageTest {

  @Inject
  WarehouseRepository warehouseRepository;

  @Test
  public void create_find_update_and_remove_flow() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-COV-001";
    w.location = "LOC";
    w.capacity = 10;
    w.stock = 2;

    warehouseRepository.create(w);

    Warehouse found = warehouseRepository.findByBusinessUnitCode("BU-COV-001");
    assertNotNull(found);
    assertEquals("LOC", found.location);

    // update existing
    found.location = "LOC-UPDATED";
    warehouseRepository.update(found);

    Warehouse updated = warehouseRepository.findByBusinessUnitCode("BU-COV-001");
    assertEquals("LOC-UPDATED", updated.location);

    // remove
    warehouseRepository.remove(found);
    Warehouse afterRemove = warehouseRepository.findByBusinessUnitCode("BU-COV-001");
    assertNull(afterRemove);
  }

  @Test
  public void update_nonexistent_throws() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "NO-BU";

    NotFoundException ex = assertThrows(NotFoundException.class, () -> warehouseRepository.update(w));
    assertTrue(ex.getMessage().contains("Warehouse not found"));
  }
}
