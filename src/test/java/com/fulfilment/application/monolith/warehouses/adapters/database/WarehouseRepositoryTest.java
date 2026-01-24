package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseRepositoryTest {

  @Inject
  WarehouseRepository repo;

  @Test
  @Transactional
  public void create_and_findByBusinessUnitCode() {
    repo.create(null); // should not throw

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-1";
    w.capacity = 10;
    w.location = "LOC";
    w.stock = 5;
    repo.create(w);

    Warehouse found = repo.findByBusinessUnitCode("BU-1");
    Assertions.assertNotNull(found);
    Assertions.assertEquals("BU-1", found.businessUnitCode);
  }

  @Test
  @Transactional
  public void update_nonExistent_throwsAnd_update_success() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "NO-BU";

    Assertions.assertThrows(IllegalArgumentException.class, () -> repo.update(w));

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "BU-2";
    w2.location = "L2";
    w2.capacity = 7;
    w2.stock = 3;
    repo.create(w2);

    w2.location = "L2-upd";
    repo.update(w2);

    Warehouse found = repo.findByBusinessUnitCode("BU-2");
    Assertions.assertNotNull(found);
    Assertions.assertEquals("L2-upd", found.location);
  }

  @Test
  @Transactional
  public void remove_null_and_remove_success() {
    repo.remove(null); // no-op

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-3";
    repo.create(w);

    Warehouse found = repo.findByBusinessUnitCode("BU-3");
    Assertions.assertNotNull(found);

    repo.remove(w);

    Warehouse after = repo.findByBusinessUnitCode("BU-3");
    Assertions.assertNull(after);
  }
}

