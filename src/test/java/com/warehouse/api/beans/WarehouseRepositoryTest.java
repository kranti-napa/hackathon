package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository realRepository;

    private WarehouseRepository spyRepo() {
        return Mockito.spy(realRepository);
    }

    // ---------- getAll ----------

    @Test
    void shouldReturnAllWarehouses() {
        WarehouseRepository repo = spyRepo();

        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "BU1";

        doReturn(List.of(db)).when(repo).listAll();

        List<Warehouse> result = repo.getAll();

        assertEquals(1, result.size());
        assertEquals("BU1", result.get(0).businessUnitCode);
    }

    // ---------- create ----------

    @Test
    void shouldCreateWarehouseSuccessfully() {
        WarehouseRepository repo = spyRepo();
        Warehouse warehouse = warehouse("BU2");

        doNothing().when(repo).persist(any(DbWarehouse.class));

        repo.create(warehouse);

        verify(repo).persist(any(DbWarehouse.class));
    }

    @Test
    void shouldIgnoreCreateWhenWarehouseIsNull() {
        WarehouseRepository repo = spyRepo();

        repo.create(null);

        verify(repo, never()).persist(any());
    }

    // ---------- update ----------

    @Test
    void shouldUpdateExistingWarehouse() {
        WarehouseRepository repo = spyRepo();
        Warehouse warehouse = warehouse("BU3");

        DbWarehouse existing = new DbWarehouse();
        existing.businessUnitCode = "BU3";

        doReturn(existing)
                .when(repo)
                .find("businessUnitCode", "BU3")
                .firstResult();

        doNothing().when(repo).persist(existing);

        repo.update(warehouse);

        verify(repo).persist(existing);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNullWarehouse() {
        WarehouseRepository repo = spyRepo();

        assertThrows(IllegalArgumentException.class,
                () -> repo.update(null));
    }

    @Test
    void shouldThrowExceptionWhenWarehouseNotFoundOnUpdate() {
        WarehouseRepository repo = spyRepo();
        Warehouse warehouse = warehouse("BU404");

        doReturn(null)
                .when(repo)
                .find("businessUnitCode", "BU404")
                .firstResult();

        assertThrows(IllegalArgumentException.class,
                () -> repo.update(warehouse));
    }

    // ---------- remove ----------

    @Test
    void shouldRemoveExistingWarehouse() {
        WarehouseRepository repo = spyRepo();
        Warehouse warehouse = warehouse("BU5");

        DbWarehouse existing = new DbWarehouse();
        existing.businessUnitCode = "BU5";

        doReturn(existing)
                .when(repo)
                .find("businessUnitCode", "BU5")
                .firstResult();

        doNothing().when(repo).delete(existing);

        repo.remove(warehouse);

        verify(repo).delete(existing);
    }

    @Test
    void shouldDoNothingWhenRemovingNullWarehouse() {
        WarehouseRepository repo = spyRepo();

        repo.remove(null);

        verify(repo, never()).delete(any());
    }

    @Test
    void shouldDoNothingWhenWarehouseNotFoundOnRemove() {
        WarehouseRepository repo = spyRepo();
        Warehouse warehouse = warehouse("BU6");

        doReturn(null)
                .when(repo)
                .find("businessUnitCode", "BU6")
                .firstResult();

        repo.remove(warehouse);

        verify(repo, never()).delete(any());
    }

    // ---------- findByBusinessUnitCode ----------

    @Test
    void shouldReturnNullWhenBuCodeIsNull() {
        WarehouseRepository repo = spyRepo();

        assertNull(repo.findByBusinessUnitCode(null));
    }

    @Test
    void shouldReturnNullWhenWarehouseNotFound() {
        WarehouseRepository repo = spyRepo();

        doReturn(null)
                .when(repo)
                .find("businessUnitCode", "BU404")
                .firstResult();

        assertNull(repo.findByBusinessUnitCode("BU404"));
    }

    @Test
    void shouldReturnWarehouseWhenFound() {
        WarehouseRepository repo = spyRepo();

        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "BU7";

        doReturn(db)
                .when(repo)
                .find("businessUnitCode", "BU7")
                .firstResult();

        Warehouse result = repo.findByBusinessUnitCode("BU7");

        assertNotNull(result);
        assertEquals("BU7", result.businessUnitCode);
    }

    // ---------- helper ----------

    private Warehouse warehouse(String bu) {
        Warehouse w = new Warehouse();
        w.businessUnitCode = bu;
        w.location = "LOC";
        w.capacity = 100;
        w.stock = 10;
        w.createdAt = Instant.now();
        return w;
    }
}
