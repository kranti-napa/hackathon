package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.testhelpers.InMemoryWarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

public class ArchiveWarehouseUseCaseTest {

  InMemoryWarehouseStore store;
  ArchiveWarehouseUseCase useCase;

  @BeforeEach
  public void setup() {
    store = new InMemoryWarehouseStore();
    useCase = new ArchiveWarehouseUseCase(store);
  }

  @Test
  public void testArchiveHappyPath() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.500";
    existing.capacity = 10;
    existing.stock = 1;
    store.create(existing);

    Warehouse toArchive = new Warehouse();
    toArchive.businessUnitCode = "MWH.500";

    useCase.archive(toArchive);

    Warehouse persisted = store.findByBusinessUnitCode("MWH.500");
    assertNotNull(persisted.archivedAt);
  }

  @Test
  public void testArchiveInvalidWarehouseThrows() {
    assertThrows(IllegalArgumentException.class, () -> useCase.archive(null));

    Warehouse w = new Warehouse();
    // missing businessUnitCode
    assertThrows(IllegalArgumentException.class, () -> useCase.archive(w));
  }

  @Test
  public void testArchiveNotFoundThrows() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "NOPE";
    assertThrows(IllegalArgumentException.class, () -> useCase.archive(w));
  }
}
