package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarehouseRepositoryIntegrationTest {

    @Inject
    WarehouseRepository repository;

    // ---------- getAll ----------

    @Test
    @Transactional
    void getAll_shouldReturnWarehouses() {
        repository.create(warehouse("BU1"));
        repository.create(warehouse("BU2"));

        List<Warehouse> warehouses = repository.getAll();

        assertTrue(warehouses.size() >= 2);
    }

    // ---------- create ----------

    @Test
    @Transactional
    void create_shouldPersistWarehouse() {
        Warehouse w = warehouse("BU3");

        repository.create(w);

        Warehouse found = repository.findByBusinessUnitCode("BU3");
        assertNotNull(found);
        assertEquals("BU3", found.businessUnitCode);
    }

    @Test
    @Transactional
    void create_shouldIgnoreNullWarehouse() {
        repository.create(null);

        // nothing to assert — coverage hits null branch
    }

    // ---------- update ----------

    @Test
    @Transactional
    void update_shouldUpdateExistingWarehouse() {
        repository.create(warehouse("BU4"));

        Warehouse updated = warehouse("BU4");
        updated.location = "UPDATED_LOC";
        updated.stock = 999;

        repository.update(updated);

        Warehouse result = repository.findByBusinessUnitCode("BU4");
        assertEquals("UPDATED_LOC", result.location);
        assertEquals(999, result.stock);
    }

    @Test
    void update_shouldThrowWhenWarehouseIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> repository.update(null));
    }

    @Test
    void update_shouldThrowWhenWarehouseNotFound() {
        Warehouse w = warehouse("BU404");

        assertThrows(IllegalArgumentException.class,
                () -> repository.update(w));
    }

    // ---------- remove ----------

    @Test
    @Transactional
    void remove_shouldDeleteExistingWarehouse() {
        repository.create(warehouse("BU5"));

        Warehouse w = warehouse("BU5");
        repository.remove(w);

        Warehouse result = repository.findByBusinessUnitCode("BU5");
        assertNull(result);
    }

    @Test
    @Transactional
    void remove_shouldIgnoreNullWarehouse() {
        repository.remove(null);
    }

    @Test
    @Transactional
    void remove_shouldDoNothingWhenWarehouseNotFound() {
        Warehouse w = warehouse("BU6");

        repository.remove(w); // should not throw
    }

    // ---------- findByBusinessUnitCode ----------

    @Test
    @Transactional
    void findByBusinessUnitCode_shouldReturnNullForNullInput() {
        assertNull(repository.findByBusinessUnitCode(null));
    }

    @Test
    @Transactional
    void findByBusinessUnitCode_shouldReturnNullWhenMissing() {
        assertNull(repository.findByBusinessUnitCode("BU_UNKNOWN"));
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
