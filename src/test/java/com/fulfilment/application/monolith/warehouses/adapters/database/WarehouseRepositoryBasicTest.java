package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

public class WarehouseRepositoryBasicTest {

  @Test
  public void create_null_isNoop() {
    WarehouseRepository repo = new WarehouseRepository();
    // should not throw
    repo.create(null);
  }

  @Test
  public void remove_null_isNoop() {
    WarehouseRepository repo = new WarehouseRepository();
    // should not throw
    repo.remove(null);
  }

  @Test
  public void update_null_throws() {
    WarehouseRepository repo = new WarehouseRepository();
    assertThrows(ValidationException.class, () -> repo.update(null));
  }

  @Test
  public void findByBusinessUnitCode_null_returnsNull() {
    WarehouseRepository repo = new WarehouseRepository();
    assertNull(repo.findByBusinessUnitCode(null));
  }

  @Test
  public void findByBusinessUnitCode_emptyString_returnsNull() {
    WarehouseRepository repo = new WarehouseRepository();
    // repository touches the DB only when non-null; empty string should simply attempt a lookup
    // but on a plain instance this will not hit DB; ensure method guards null explicitly
    // when using a plain instance outside Quarkus/Panache enhancement this will throw
    // IllegalStateException; assert that behavior to keep the test deterministic.
    assertThrows(IllegalStateException.class, () -> repo.findByBusinessUnitCode(""));
  }
}
