package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.testhelpers.InMemoryWarehouseStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Transactional
public class WarehouseResourceImplTest {

  @Inject
  WarehouseResourceImpl resource;

  @Inject
  WarehouseRepository warehouseRepository;

  static class TestWarehouseRepository extends WarehouseRepository {
    private final WarehouseStore delegate;

    TestWarehouseRepository(WarehouseStore delegate) {
      this.delegate = delegate;
    }

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

  WarehouseResourceImpl unitResource;

  @BeforeEach
  public void setupUnit() throws Exception {
    unitResource = new WarehouseResourceImpl();
    WarehouseStore store = new InMemoryWarehouseStore();
    TestWarehouseRepository repo = new TestWarehouseRepository(store);
    CreateWarehouseUseCase createUseCase = new CreateWarehouseUseCase(store, new LocationGateway());
    ReplaceWarehouseUseCase replaceUseCase = new ReplaceWarehouseUseCase(store);
    ArchiveWarehouseUseCase archiveUseCase = new ArchiveWarehouseUseCase(store);

    Field f = WarehouseResourceImpl.class.getDeclaredField("warehouseRepository");
    f.setAccessible(true);
    f.set(unitResource, repo);

    Field createField = WarehouseResourceImpl.class.getDeclaredField("createWarehouseUseCase");
    createField.setAccessible(true);
    createField.set(unitResource, createUseCase);

    Field replaceField = WarehouseResourceImpl.class.getDeclaredField("replaceWarehouseUseCase");
    replaceField.setAccessible(true);
    replaceField.set(unitResource, replaceUseCase);

    Field archiveField = WarehouseResourceImpl.class.getDeclaredField("archiveWarehouseUseCase");
    archiveField.setAccessible(true);
    archiveField.set(unitResource, archiveUseCase);
  }

  // ---------- ENDPOINT TESTS ----------

  @Test
  public void testSimpleListWarehouses() {
    final String path = "warehouse";

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void testSimpleCheckingArchivingWarehouses() {
    final String path = "warehouse";

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(
            containsString("MWH.001"),
            containsString("MWH.012"),
            containsString("MWH.023"),
            containsString("ZWOLLE-001"),
            containsString("AMSTERDAM-001"),
            containsString("TILBURG-001"));

    given().when().delete(path + "/MWH.001").then().statusCode(204);

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(
        containsString("ZWOLLE-001"),
        containsString("AMSTERDAM-001"),
        containsString("TILBURG-001"));
  }

  // ---------- RESOURCE TESTS ----------

  @Test
  public void testListAndCreateAndGet() {
    List<com.warehouse.api.beans.Warehouse> list = unitResource.listAllWarehousesUnits();
    assertNotNull(list);

    com.warehouse.api.beans.Warehouse api = new com.warehouse.api.beans.Warehouse();
    api.setBusinessUnitCode("B-1");
    api.setLocation("AMSTERDAM-001");
    api.setCapacity(10);
    api.setStock(0);

    unitResource.createANewWarehouseUnit(api);

    com.warehouse.api.beans.Warehouse fetched = unitResource.getAWarehouseUnitByID("B-1");
    assertEquals("B-1", fetched.getBusinessUnitCode());
  }

  @Test
  public void testArchiveFlow() {
    com.warehouse.api.beans.Warehouse api = new com.warehouse.api.beans.Warehouse();
    api.setBusinessUnitCode("B-ARCH");
    api.setLocation("AMSTERDAM-001");
    api.setCapacity(5);
    api.setStock(0);

    unitResource.createANewWarehouseUnit(api);

    unitResource.archiveAWarehouseUnitByID("B-ARCH");

    List<com.warehouse.api.beans.Warehouse> list = unitResource.listAllWarehousesUnits();
    assertNotNull(list);
  }

  @Test
  public void testReplace_happyPath_updatesExisting() {
    com.warehouse.api.beans.Warehouse api = new com.warehouse.api.beans.Warehouse();
    api.setBusinessUnitCode("BR-10");
    api.setLocation("L-OLD");
    api.setCapacity(10);
    api.setStock(1);

    resource.createANewWarehouseUnit(api);

    com.warehouse.api.beans.Warehouse replacement = new com.warehouse.api.beans.Warehouse();
    replacement.setLocation("L-NEW");
    replacement.setCapacity(20);
    replacement.setStock(2);

    com.warehouse.api.beans.Warehouse out = resource.replaceTheCurrentActiveWarehouse("BR-10", replacement);
    assertNotNull(out);

    var found = warehouseRepository.findByBusinessUnitCode("BR-10");
    assertNotNull(found);
    assertEquals("L-NEW", found.location);
    assertEquals(2, found.stock.intValue());
  }

  @Test
  public void testGetAWarehouseUnitByID_notFound_throws() {
    assertThrows(NotFoundException.class, () -> resource.getAWarehouseUnitByID("NO-SUCH-BU"));
  }

  @Test
  public void testReplace_nonExisting_throws() {
    com.warehouse.api.beans.Warehouse replacement = new com.warehouse.api.beans.Warehouse();
    replacement.setLocation("x");
    assertThrows(NotFoundException.class, () -> resource.replaceTheCurrentActiveWarehouse("NO-BU", replacement));
  }
}
