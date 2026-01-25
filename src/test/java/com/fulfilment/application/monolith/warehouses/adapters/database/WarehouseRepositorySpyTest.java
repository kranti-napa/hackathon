package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class WarehouseRepositorySpyTest {

    @Inject
    WarehouseRepository realRepository;

    private WarehouseRepository spyRepo() {
        return Mockito.spy(realRepository);
    }

    // ---------- getAll ----------

    @Test
    void getAll_shouldMapEntities() {
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
    void create_shouldPersistWarehouse() {
        WarehouseRepository repo = spyRepo();
        Warehouse warehouse = warehouse("BU2");

        doNothing().when(repo).persist(Mockito.<DbWarehouse>any());

        repo.create(warehouse);

        verify(repo).persist(Mockito.<DbWarehouse>any());
    }

    @Test
    void create_shouldIgnoreNullWarehouse() {
        WarehouseRepository repo = spyRepo();

        repo.create(null);

        verify(repo, never()).persist(Mockito.<DbWarehouse>any());
    }

    // ---------- update ----------

    @Test
    void update_shouldPersistWhenFound() {
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
    void update_shouldThrowWhenNullWarehouse() {
        WarehouseRepository repo = spyRepo();

        assertThrows(IllegalArgumentException.class,
                () -> repo.update(null));
    }

    @Test
    void update_shouldThrowWhenWarehouseNotFound() {
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
    void remove_shouldDeleteWhenFound() {
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
    void remove_shouldIgnoreNullWarehouse() {
        WarehouseRepository repo = spyRepo();

        repo.remove(null);

        verify(repo, never()).delete(any());
    }

    @Test
    void remove_shouldDoNothingWhenNotFound() {
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
    void findByBusinessUnitCode_shouldReturnNullForNullInput() {
        WarehouseRepository repo = spyRepo();

        assertNull(repo.findByBusinessUnitCode(null));
    }

    @Test
    void findByBusinessUnitCode_shouldReturnNullWhenMissing() {
        WarehouseRepository repo = spyRepo();

        doReturn(null)
            .when(repo)
            .find("businessUnitCode", "BU404")
            .firstResult();

        assertNull(repo.findByBusinessUnitCode("BU404"));
    }

    @Test
    void findByBusinessUnitCode_shouldMapWhenFound() {
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
        w.createdAt = LocalDateTime.now();
        w.archivedAt = null;
        return w;
    }
}

