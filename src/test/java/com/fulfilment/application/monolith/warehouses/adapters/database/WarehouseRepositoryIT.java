package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WarehouseRepositoryIT {

  @Inject
  WarehouseRepository repository;

  @Test
  @Transactional
  public void crudFlow() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-IT-1";
    w.location = "LOC-IT";
    w.capacity = 100;
    w.stock = 10;

    repository.create(w);

    Warehouse found = repository.findByBusinessUnitCode("BU-IT-1");
    assertNotNull(found);
    assertEquals("LOC-IT", found.location);

    w.location = "LOC-IT-2";
    w.stock = 20;
    repository.update(w);

    Warehouse after = repository.findByBusinessUnitCode("BU-IT-1");
    assertNotNull(after);
    assertEquals("LOC-IT-2", after.location);
    assertEquals(20, after.stock.intValue());

    repository.remove(w);
    assertNull(repository.findByBusinessUnitCode("BU-IT-1"));
  }
}
