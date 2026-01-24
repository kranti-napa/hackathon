package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Transactional
public class WarehouseRepositoryCoverageIT {

  @Inject
  WarehouseRepository warehouseRepository;

  @Test
  public void testCreateAndFindByBusinessUnitCode() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-IT-1";
    w.location = "LocX";
    w.capacity = 100;
    w.stock = 5;

    warehouseRepository.create(w);

    Warehouse found = warehouseRepository.findByBusinessUnitCode("BU-IT-1");
    assertNotNull(found);
    assertEquals("BU-IT-1", found.businessUnitCode);
  }

  @Test
  public void testUpdate_happyPath() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-IT-2";
    w.location = "L1";
    w.capacity = 50;
    w.stock = 1;

    warehouseRepository.create(w);

    w.location = "L2";
    w.stock = 10;
    warehouseRepository.update(w);

    Warehouse found = warehouseRepository.findByBusinessUnitCode("BU-IT-2");
    assertNotNull(found);
    assertEquals("L2", found.location);
    assertEquals(10, found.stock.intValue());
  }

  @Test
  public void testUpdate_nonExisting_throwsIllegalArgumentException() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "NO-SUCH-BU";

    assertThrows(IllegalArgumentException.class, () -> warehouseRepository.update(w));
  }

  @Test
  public void testRemove_existing_and_nonExisting() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-IT-3";
    w.location = "L3";
    w.capacity = 20;
    w.stock = 2;

    warehouseRepository.create(w);

    warehouseRepository.remove(w);
    assertNull(warehouseRepository.findByBusinessUnitCode("BU-IT-3"));

    // removing again should be a no-op (not throw)
    warehouseRepository.remove(w);
  }
}
