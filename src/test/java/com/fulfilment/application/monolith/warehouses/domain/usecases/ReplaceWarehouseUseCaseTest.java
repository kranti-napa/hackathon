package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.usecases.testhelpers.InMemoryWarehouseStore;
import com.fulfilment.application.monolith.location.LocationGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  InMemoryWarehouseStore store;
  ArchiveWarehouseOperation archiveOperation;
  LocationGateway locationGateway;
  ReplaceWarehouseUseCase useCase;

  @BeforeEach
  public void setup() {
    store = new InMemoryWarehouseStore();
    archiveOperation = new ArchiveWarehouseUseCase(store);
    locationGateway = new LocationGateway();
    useCase = new ReplaceWarehouseUseCase(store, archiveOperation, locationGateway);
  }

  @Test
  public void testReplaceHappyPath() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.900";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 10;
    existing.stock = 5;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.900";
    replacement.location = "AMSTERDAM-001";
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
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 10;
    replacement.stock = 0;

    assertThrows(NotFoundException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceStockMismatchThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.800";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 10;
    existing.stock = 2;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.800";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 12;
    replacement.stock = 3; // different

    assertThrows(ConflictException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceInvalidCapacityThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.700";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 10;
    existing.stock = 1;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.700";
    replacement.location = "AMSTERDAM-001";
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
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 10;
    replacement.stock = 1;

    assertThrows(ValidationException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceNullCapacityThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.701";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 10;
    existing.stock = 1;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.701";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = null;
    replacement.stock = 1;

    assertThrows(ValidationException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceCapacityInsufficientForStockThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.702";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 10;
    existing.stock = 5;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.702";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 3; // less than stock of 5
    replacement.stock = 5;

    assertThrows(ValidationException.class, () -> useCase.replace(replacement));
  }

  @Test
  public void testReplaceCapacityEqualToStockSucceeds() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.703";
    existing.location = "AMSTERDAM-001";
    existing.capacity = 10;
    existing.stock = 5;
    store.create(existing);

    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.703";
    replacement.location = "AMSTERDAM-001";
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
    existing.location = "AMSTERDAM-001";
    existing.capacity = 100;
    existing.stock = 100;
    store.create(existing);

    // Try to replace with warehouse that can only hold 50 items
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.704";
    replacement.location = "AMSTERDAM-001";
    replacement.capacity = 50; // INSUFFICIENT for existing stock of 100
    replacement.stock = 100;

    assertThrows(
        ValidationException.class,
        () -> useCase.replace(replacement),
        "Should reject replacement with capacity insufficient for existing stock"
    );
  }

  @Test
  public void testReplaceLocationCapacityExceededThrows() {
    // ZWOLLE-001 has maxCapacity of 100
    // Create another warehouse at same location with 80 capacity
    Warehouse other = new Warehouse();
    other.businessUnitCode = "MWH.OTHER";
    other.location = "ZWOLLE-001";
    other.capacity = 80;
    other.stock = 0;
    store.create(other);

    // Create existing warehouse with capacity 10 (total: 90)
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "MWH.705";
    existing.location = "ZWOLLE-001";
    existing.capacity = 10;
    existing.stock = 5;
    store.create(existing);

    // Try to replace with capacity 30, which would make total 100 (80 + 30)
    // This should still succeed since 100 <= 100
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.705";
    replacement.location = "ZWOLLE-001";
    replacement.capacity = 20; // 80 + 20 = 100 (at limit, OK)
    replacement.stock = 5;

    useCase.replace(replacement); // Should succeed

    // Now try capacity 21, which would exceed
    Warehouse existing2 = store.findByBusinessUnitCode("MWH.705");
    Warehouse tooLarge = new Warehouse();
    tooLarge.businessUnitCode = "MWH.705";
    tooLarge.location = "ZWOLLE-001";
    tooLarge.capacity = 21; // 80 + 21 = 101 > 100
    tooLarge.stock = 5;

    assertThrows(
        ConflictException.class,
        () -> useCase.replace(tooLarge),
        "Should reject replacement that exceeds location max capacity"
    );
  }
}
