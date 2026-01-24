package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.testhelpers.InMemoryWarehouseStore;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WarehouseResourceImplTest {

  WarehouseResourceImpl resource;
  InMemoryWarehouseStore store;

  static class TestWarehouseRepository extends com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository {
    private final WarehouseStore delegate = new InMemoryWarehouseStore();

    @Override
    public java.util.List<com.fulfilment.application.monolith.warehouses.domain.models.Warehouse> getAll() {
      return delegate.getAll();
    }

    @Override
    public void create(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
      delegate.create(warehouse);
    }

    @Override
    public void update(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
      delegate.update(warehouse);
    }
  }

  @BeforeEach
  public void setup() throws Exception {
    resource = new WarehouseResourceImpl();
    // inject test repo via reflection (field has package access)
    TestWarehouseRepository repo = new TestWarehouseRepository();
    Field f = WarehouseResourceImpl.class.getDeclaredField("warehouseRepository");
    f.setAccessible(true);
    f.set(resource, repo);
  }

  @Test
  public void testListAndCreateAndGet() {
    List<com.warehouse.api.beans.Warehouse> list = resource.listAllWarehousesUnits();
    assertNotNull(list);

    com.warehouse.api.beans.Warehouse api = new com.warehouse.api.beans.Warehouse();
    api.setBusinessUnitCode("B-1");
    api.setLocation("AMSTERDAM-001");
    api.setCapacity(10);
    api.setStock(0);

    resource.createANewWarehouseUnit(api);

    com.warehouse.api.beans.Warehouse fetched = resource.getAWarehouseUnitByID("B-1");
    assertEquals("B-1", fetched.getBusinessUnitCode());
  }

  @Test
  public void testArchiveFlow() {
    com.warehouse.api.beans.Warehouse api = new com.warehouse.api.beans.Warehouse();
    api.setBusinessUnitCode("B-ARCH");
    api.setLocation("AMSTERDAM-001");
    api.setCapacity(5);
    api.setStock(0);

    resource.createANewWarehouseUnit(api);

    resource.archiveAWarehouseUnitByID("B-ARCH");

    // now listing should not throw and archived warehouse should have archivedAt set in the domain
    List<com.warehouse.api.beans.Warehouse> list = resource.listAllWarehousesUnits();
    assertNotNull(list);
  }
}
