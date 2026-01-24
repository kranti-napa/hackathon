package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.testhelpers.InMemoryWarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  InMemoryWarehouseStore store;
  ReplaceWarehouseUseCase useCase;

  @BeforeEach
  public void setup() {
    store = new InMemoryWarehouseStore();
    useCase = new ReplaceWarehouseUseCase(store);
  }

  @Test
  public void testReplaceHappyPath() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.900";
    existing.capacity = 10;
    existing.stock = 5;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.900";
    replacement.capacity = 20; // allowed
    replacement.stock = 5; // must match existing

    useCase.replace(replacement);

    Warehouse persisted = store.findByBusinessUnitCode("MWH.900");
    assertNotNull(persisted);
    assertEquals(20, persisted.capacity.intValue());
    assertEquals(5, persisted.stock.intValue());
  }

  @Test
  public void testReplaceNotFoundThrows() {
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "NOPE";
    replacement.capacity = 10;
    replacement.stock = 0;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceStockMismatchThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.800";
    existing.capacity = 10;
    existing.stock = 2;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.800";
    replacement.capacity = 12;
    replacement.stock = 3; // different

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceInvalidCapacityThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.700";
    existing.capacity = 10;
    existing.stock = 1;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.700";
    replacement.capacity = 0; // invalid
    replacement.stock = 1;

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }
}
