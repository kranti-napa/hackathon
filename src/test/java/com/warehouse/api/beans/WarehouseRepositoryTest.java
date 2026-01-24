package com.fulfilment.application.monolith.warehouse.api.beans;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

@QuarkusTest
class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository repository;

    @AfterEach
    void tearDown() {
        PanacheMock.reset(DbWarehouse.class);
    }

    // ---------- getAll ----------

    @Test
    void shouldReturnAllWarehouses() {
        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "BU1";
        db.location = "BLR";
        db.capacity = 100;
        db.stock = 10;

        PanacheMock.mock(DbWarehouse.class);
        when(DbWarehouse.listAll()).thenReturn(List.of(db));

        List<Warehouse> result = repository.getAll();

        assertEquals(1, result.size());
        assertEquals("BU1", result.get(0).businessUnitCode);
    }

    // ---------- create ----------

    @Test
    void shouldCreateWarehouseSuccessfully() {
        Warehouse warehouse = buildWarehouse("BU2");

        PanacheMock.mock(DbWarehouse.class);

        repository.create(warehouse);

        PanacheMock.verify(DbWarehouse.class).persist(any(DbWarehouse.class));
    }

    @Test
    void shouldIgnoreCreateWhenWarehouseIsNull() {
        PanacheMock.mock(DbWarehouse.class);

        repository.create(null);

        PanacheMock.verify(DbWarehouse.class, never()).persist(any());
    }

    // ---------- update ----------

    @Test
    void shouldUpdateExistingWarehouse() {
        Warehouse warehouse = buildWarehouse("BU3");

        DbWarehouse existing = new DbWarehouse();
        existing.businessUnitCode = "BU3";

        PanacheMock.mock(DbWarehouse.class);
        when(DbWarehouse.find("businessUnitCode", "BU3").firstResult())
                .thenReturn(existing);

        repository.update(warehouse);

        PanacheMock.verify(DbWarehouse.class).persist(existing);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNullWarehouse() {
        assertThrows(IllegalArgumentException.class,
                () -> repository.update(null));
    }

    @Test
    void shouldThrowExceptionWhenWarehouseNotFoundOnUpdate() {
        Warehouse warehouse = buildWarehouse("BU404");

        PanacheMock.mock(DbWarehouse.class);
        when(DbWarehouse.find("businessUnitCode", "BU404").firstResult())
                .thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> repository.update(warehouse));
    }

    // ---------- remove ----------

    @Test
    void shouldRemoveExistingWarehouse() {
        Warehouse warehouse = buildWarehouse("BU5");

        DbWarehouse existing = new DbWarehouse();
        existing.businessUnitCode = "BU5";

        PanacheMock.mock(DbWarehouse.class);
        when(DbWarehouse.find("businessUnitCode", "BU5").firstResult())
                .thenReturn(existing);

        repository.remove(warehouse);

        PanacheMock.verify(DbWarehouse.class).delete(existing);
    }

    @Test
    void shouldDoNothingWhenRemovingNullWarehouse() {
        PanacheMock.mock(DbWarehouse.class);

        repository.remove(null);

        PanacheMock.verify(DbWarehouse.class, never()).delete(any());
    }

    @Test
    void shouldDoNothingWhenWarehouseNotFoundOnRemove() {
        Warehouse warehouse = buildWarehouse("BU6");

        PanacheMock.mock(DbWarehouse.class);
        when(DbWarehouse.find("businessUnitCode", "BU6").firstResult())
                .thenReturn(null);

        repository.remove(warehouse);

        PanacheMock.verify(DbWarehouse.class, never()).delete(any());
    }

    // ---------- findByBusinessUnitCode ----------

    @Test
    void shouldReturnNullWhenBuCodeIsNull() {
        Warehouse result = repository.findByBusinessUnitCode(null);
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenWarehouseNotFound() {
        PanacheMock.mock(DbWarehouse.class);
        when(DbWarehouse.find("businessUnitCode", "BU404").firstResult())
                .thenReturn(null);

        Warehouse result = repository.findByBusinessUnitCode("BU404");

        assertNull(result);
    }

    @Test
    void shouldReturnWarehouseWhenFound() {
        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "BU7";
        db.location = "HYD";
        db.capacity = 50;
        db.stock = 5;

        PanacheMock.mock(DbWarehouse.class);
        when(DbWarehouse.find("businessUnitCode", "BU7").firstResult())
                .thenReturn(db);

        Warehouse result = repository.findByBusinessUnitCode("BU7");

        assertNotNull(result);
        assertEquals("BU7", result.businessUnitCode);
    }

    // ---------- helper ----------

    private Warehouse buildWarehouse(String bu) {
        Warehouse w = new Warehouse();
        w.businessUnitCode = bu;
        w.location = "LOC";
        w.capacity = 100;
        w.stock = 10;
        w.createdAt = Instant.now();
        return w;
    }
}
