package com.warehouse.api.beans;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WarehouseRepositroyTest {

    @InjectMocks
    WarehouseRepository repository;

    @Mock
    PanacheQuery<DbWarehouse> query;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // --------------------------------------------------
    // getAll()
    // --------------------------------------------------

    @Test
    void getAll_shouldReturnMappedWarehouses() {
        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "BU1";
        db.capacity = 100;
        db.stock = 20;

        WarehouseRepository spyRepo = spy(repository);
        doReturn(List.of(db)).when(spyRepo).listAll();

        List<Warehouse> result = spyRepo.getAll();

        assertEquals(1, result.size());
        assertEquals("BU1", result.get(0).businessUnitCode);
        assertEquals(100, result.get(0).capacity);
        assertEquals(20, result.get(0).stock);
    }

    // --------------------------------------------------
    // create()
    // --------------------------------------------------

    @Test
    void create_shouldDoNothingWhenWarehouseIsNull() {
        repository.create(null);
        verify(repository, never()).persist(any(DbWarehouse.class));
    }

    // --------------------------------------------------
    // update()
    // --------------------------------------------------

    @Test
    void update_shouldThrowWhenWarehouseNotFound() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU404";

        WarehouseRepository spyRepo = spy(repository);
        doReturn(query).when(spyRepo).find("businessUnitCode", "BU404");
        when(query.firstResult()).thenReturn(null);

        assertThrows(RuntimeException.class, () -> spyRepo.update(warehouse));
    }

    // --------------------------------------------------
    // remove()
    // --------------------------------------------------

    @Test
    void remove_shouldDoNothingWhenWarehouseNotFound() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "BU404";

        WarehouseRepository spyRepo = spy(repository);
        doReturn(query).when(spyRepo).find("businessUnitCode", "BU404");
        when(query.firstResult()).thenReturn(null);

        spyRepo.remove(warehouse);

        verify(spyRepo, never()).delete(any());
    }

    // --------------------------------------------------
    // findByBusinessUnitCode()
    // --------------------------------------------------

    @Test
    void findByBusinessUnitCode_shouldReturnWarehouseWhenFound() {
        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "BU1";
        db.capacity = 50;
        db.stock = 10;

        WarehouseRepository spyRepo = spy(repository);
        doReturn(query).when(spyRepo).find("businessUnitCode", "BU1");
        when(query.firstResult()).thenReturn(db);

        Warehouse result = spyRepo.findByBusinessUnitCode("BU1");

        assertNotNull(result);
        assertEquals("BU1", result.businessUnitCode);
        assertEquals(50, result.capacity);
        assertEquals(10, result.stock);
    }

    @Test
    void findByBusinessUnitCode_shouldReturnNullWhenInputIsNull() {
        Warehouse result = repository.findByBusinessUnitCode(null);
        assertNull(result);
    }
}
