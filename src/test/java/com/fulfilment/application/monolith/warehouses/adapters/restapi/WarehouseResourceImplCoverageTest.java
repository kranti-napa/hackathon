package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.warehouse.api.beans.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Transactional
public class WarehouseResourceImplCoverageTest {

  @Inject
  WarehouseResourceImpl resource;

  @Inject
  WarehouseRepository warehouseRepository;

  @Test
  public void testReplace_happyPath_updatesExisting() {
    Warehouse api = new Warehouse();
    api.setBusinessUnitCode("BR-10");
    api.setLocation("L-OLD");
    api.setCapacity(10);
    api.setStock(1);

    // create
    resource.createANewWarehouseUnit(api);

    // prepare replacement payload (note: replaceTheCurrentActiveWarehouse uses the path param BU)
    Warehouse replacement = new Warehouse();
    replacement.setLocation("L-NEW");
    replacement.setCapacity(20);
    replacement.setStock(2);

    Warehouse out = resource.replaceTheCurrentActiveWarehouse("BR-10", replacement);
    assertNotNull(out);

    var found = warehouseRepository.findByBusinessUnitCode("BR-10");
    assertNotNull(found);
    assertEquals("L-NEW", found.location);
    assertEquals(2, found.stock.intValue());
  }

  @Test
  public void testGetAWarehouseUnitByID_notFound_throws() {
    assertThrows(IllegalArgumentException.class, () -> resource.getAWarehouseUnitByID("NO-SUCH-BU"));
  }

  @Test
  public void testArchive_happyPath_setsArchivedAt() {
    Warehouse api = new Warehouse();
    api.setBusinessUnitCode("BR-11");
    api.setLocation("L-A");
    api.setCapacity(5);
    api.setStock(0);

    resource.createANewWarehouseUnit(api);

    resource.archiveAWarehouseUnitByID("BR-11");

    var found = warehouseRepository.findByBusinessUnitCode("BR-11");
    assertNotNull(found);
    assertNotNull(found.archivedAt);
  }

  @Test
  public void testReplace_nonExisting_throws() {
    Warehouse replacement = new Warehouse();
    replacement.setLocation("x");
    assertThrows(IllegalArgumentException.class, () -> resource.replaceTheCurrentActiveWarehouse("NO-BU", replacement));
  }
}
