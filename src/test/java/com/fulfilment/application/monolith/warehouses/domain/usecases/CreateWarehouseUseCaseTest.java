package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.testhelpers.InMemoryWarehouseStore;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  InMemoryWarehouseStore store;
  CreateWarehouseUseCase useCase;

  @BeforeEach
  public void setup() {
    store = new InMemoryWarehouseStore();
    useCase = new CreateWarehouseUseCase(store, new LocationGateway());
  }

  @Test
  public void testCreateHappyPath() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-100";
    w.location = "AMSTERDAM-001"; // exists in LocationGateway
    w.capacity = 10;
    w.stock = 0;

    useCase.create(w);

    Warehouse persisted = store.findByBusinessUnitCode("BU-100");
    assertNotNull(persisted);
    assertNotNull(persisted.createdAt);
    assertNull(persisted.archivedAt);
  }

  @Test
  public void testCreateDuplicateBusinessUnitThrows() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "BU-EX";
    store.create(existing);

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-EX";
    w.location = "AMSTERDAM-001";
    w.capacity = 5;
    w.stock = 1;

    assertThrows(ConflictException.class, () -> useCase.create(w));
  }

  @Test
  public void testCreateInvalidCapacityThrows() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-NEG";
    w.location = "AMSTERDAM-001";
    w.capacity = 0;
    w.stock = 1;

    assertThrows(ValidationException.class, () -> useCase.create(w));
  }

  @Test
  public void testCreateInvalidStockThrows() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-NEG2";
    w.location = "AMSTERDAM-001";
    w.capacity = 5;
    w.stock = -1;

    assertThrows(ValidationException.class, () -> useCase.create(w));
  }

  @Test
  public void testCreateMaxWarehousesExceededThrows() {
    // LocationGateway limits ZWOLLE-001 to 1 warehouse in static data
    Warehouse first = new Warehouse();
    first.businessUnitCode = "MWH.001";
    first.location = "ZWOLLE-001";
    first.capacity = 10;
    first.stock = 0;
    store.create(first);

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-NEW";
    w.location = "ZWOLLE-001";
    w.capacity = 5;
    w.stock = 0;

    assertThrows(ConflictException.class, () -> useCase.create(w));
  }
}
