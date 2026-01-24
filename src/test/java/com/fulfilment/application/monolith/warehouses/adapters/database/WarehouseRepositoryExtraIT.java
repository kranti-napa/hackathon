package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WarehouseRepositoryExtraIT {

  @Inject
  WarehouseRepository repository;

  @Test
  public void create_and_find_and_remove() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-X-1";
    w.location = "LX";
    w.capacity = 10;
    w.stock = 2;

    repository.create(w);

    Warehouse found = repository.findByBusinessUnitCode("BU-X-1");
    assertNotNull(found);
    assertEquals("LX", found.location);

    repository.remove(w);
    assertNull(repository.findByBusinessUnitCode("BU-X-1"));
  }

  @Test
  public void find_null_returns_null() {
    assertNull(repository.findByBusinessUnitCode(null));
  }
}
