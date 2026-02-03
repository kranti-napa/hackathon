package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.usecases.testhelpers.InMemoryWarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  InMemoryWarehouseStore store;
  ArchiveWarehouseOperation archiveOperation;
  ReplaceWarehouseUseCase useCase;

  @BeforeEach
  public void setup() {
    store = new InMemoryWarehouseStore();
    archiveOperation = new ArchiveWarehouseUseCase(store);
    useCase = new ReplaceWarehouseUseCase(store, archiveOperation);
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

    // Verify new warehouse is created with updated capacity
    Warehouse persisted = store.findByBusinessUnitCode("MWH.900");
    assertNotNull(persisted);
    assertEquals(20, persisted.capacity.intValue());
    assertEquals(5, persisted.stock.intValue());
    assertNull(persisted.archivedAt, "New warehouse should not be archived");

    // Verify old warehouse is archived (history preserved)
    Warehouse archived = store.getAll().stream()
        .filter(w -> w.businessUnitCode.equals("MWH.900") && w.archivedAt != null)
        .findFirst()
        .orElse(null);
    assertNotNull(archived, "Old warehouse should be archived");
    assertEquals(10, archived.capacity.intValue(), "Archived warehouse should have original capacity");
    assertEquals(5, archived.stock.intValue());
  }

  @Test
  public void testReplaceNotFoundThrows() {
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "NOPE";
    replacement.capacity = 10;
    replacement.stock = 0;

    assertThrows(NotFoundException.class, () -> useCase.replace(replacement));
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

    assertThrows(ConflictException.class, () -> useCase.replace(replacement));
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

    assertThrows(ValidationException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceNullWarehouseThrows() {
    assertThrows(ValidationException.class, () -> useCase.replace(null));
  }

  @Test
  public void testReplaceNullBusinessUnitThrows() {
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = null;
    replacement.capacity = 10;
    replacement.stock = 1;

    assertThrows(ValidationException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceNullCapacityThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.701";
    existing.capacity = 10;
    existing.stock = 1;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.701";
    replacement.capacity = null;
    replacement.stock = 1;

    assertThrows(ValidationException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceCapacityInsufficientForStockThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.702";
    existing.capacity = 10;
    existing.stock = 5;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.702";
    replacement.capacity = 3; // less than stock of 5
    replacement.stock = 5;

    assertThrows(ValidationException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceCapacityEqualToStockSucceeds() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.703";
    existing.capacity = 10;
    existing.stock = 5;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.703";
    replacement.capacity = 5; // equal to stock
    replacement.stock = 5;

    useCase.replace(replacement);

    Warehouse persisted = store.findByBusinessUnitCode("MWH.703");
    assertNotNull(persisted);
    assertEquals(5, persisted.capacity.intValue());
  }

  @Test
  public void testReplaceWithInsufficientCapacityForExistingStockThrows() {
    // Scenario: Warehouse holding 100 items with capacity 100
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.704";
    existing.capacity = 100;
    existing.stock = 100;
    store.create(existing);

    // Try to replace with warehouse that can only hold 50 items
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.704";
    replacement.capacity = 50; // INSUFFICIENT for existing stock of 100
    replacement.stock = 100;

    assertThrows(
        ValidationException.class,
        () -> useCase.replace(replacement),
        "Should reject replacement with capacity insufficient for existing stock"
    );
  }
}
