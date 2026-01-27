package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

@QuarkusTest
class WarehouseRepositoryTest {

  @Inject
  WarehouseRepository repository;

  @Test
  @Transactional
  void update_nonExistent_throwsAnd_update_success() {
    Warehouse missing = new Warehouse();
    missing.businessUnitCode = "BU-MISSING";

    assertThrows(NotFoundException.class, () -> repository.update(missing));

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-EXISTING";
    w.location = "LOC";
    w.capacity = 10;
    w.stock = 1;

    repository.create(w);

    Warehouse updated = new Warehouse();
    updated.businessUnitCode = "BU-EXISTING";
    updated.location = "LOC-UPDATED";
    updated.capacity = 20;
    updated.stock = 5;

    repository.update(updated);

    Warehouse found = repository.findByBusinessUnitCode("BU-EXISTING");
    assertNotNull(found);
    assertEquals("LOC-UPDATED", found.location);
    assertEquals(20, found.capacity);
    assertEquals(5, found.stock);
  }
}
