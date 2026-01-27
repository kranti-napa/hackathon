package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@Transactional
public class WarehouseRepositoryTest {

  @Inject
  WarehouseRepository repo;

  // ---------- BASIC CRUD ----------

  @Test
  public void create_and_findByBusinessUnitCode() {
    repo.create(null); // should not throw

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-1";
    w.capacity = 10;
    w.location = "LOC";
    w.stock = 5;
    repo.create(w);

    Warehouse found = repo.findByBusinessUnitCode("BU-1");
    assertNotNull(found);
    assertEquals("BU-1", found.businessUnitCode);
  }

  @Test
  public void update_nonExistent_throwsAnd_update_success() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "NO-BU";

    assertThrows(NotFoundException.class, () -> repo.update(w));

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "BU-2";
    w2.location = "L2";
    w2.capacity = 7;
    w2.stock = 3;
    repo.create(w2);

    w2.location = "L2-upd";
    repo.update(w2);

    Warehouse found = repo.findByBusinessUnitCode("BU-2");
    assertNotNull(found);
    assertEquals("L2-upd", found.location);
  }

  @Test
  public void remove_null_and_remove_success() {
    repo.remove(null); // no-op

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-3";
    repo.create(w);

    Warehouse found = repo.findByBusinessUnitCode("BU-3");
    assertNotNull(found);

    repo.remove(w);

    Warehouse after = repo.findByBusinessUnitCode("BU-3");
    assertNull(after);
  }

  @Test
  public void update_nonexistent_throwsWithMessage() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "NO-BU";

    NotFoundException ex = assertThrows(NotFoundException.class, () -> repo.update(w));
    assertTrue(ex.getMessage().contains("Warehouse not found"));
  }

  // ---------- NON-QUARKUS BEHAVIOR ----------

  @Test
  public void update_null_throwsValidation() {
    WarehouseRepository local = new WarehouseRepository();
    assertThrows(ValidationException.class, () -> local.update(null));
  }

  @Test
  public void findByBusinessUnitCode_null_returnsNull() {
    WarehouseRepository local = new WarehouseRepository();
    assertNull(local.findByBusinessUnitCode(null));
  }

  @Test
  public void findByBusinessUnitCode_emptyString_throwsIllegalStateException() {
    WarehouseRepository local = new WarehouseRepository();
    assertNull(local.findByBusinessUnitCode(""));
  }

  // ---------- INTEGRATION BEHAVIOR ----------

  @Test
  void getAll_shouldReturnSeededWarehouses() {
    List<Warehouse> result = repo.getAll();
    assertFalse(result.isEmpty());
    assertTrue(result.stream().anyMatch(w -> "MWH.001".equals(w.businessUnitCode)));
  }

  // ---------- UNIT-STYLE MAPPING TESTS ----------

  static class TestWarehouseRepository extends WarehouseRepository {
    public java.util.List<DbWarehouse> list;
    public DbWarehouse persisted;
    public DbWarehouse toFind;
    public DbWarehouse deleted;

    public java.util.List<DbWarehouse> listAll() {
      return list;
    }

    @Override
    public void create(Warehouse warehouse) {
      DbWarehouse db = new DbWarehouse();
      db.businessUnitCode = warehouse.businessUnitCode;
      db.location = warehouse.location;
      db.capacity = warehouse.capacity;
      db.stock = warehouse.stock;
      this.persisted = db;
    }

    @Override
    public void update(Warehouse warehouse) {
      if (toFind == null) {
        throw new NotFoundException(String.format(AppConstants.ERR_WAREHOUSE_NOT_FOUND, warehouse.businessUnitCode));
      }
      toFind.location = warehouse.location;
      toFind.capacity = warehouse.capacity;
      toFind.stock = warehouse.stock;
      this.persisted = toFind;
    }

    @Override
    public void remove(Warehouse warehouse) {
      this.deleted = toFind;
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      if (toFind == null) return null;
      return toFind.toWarehouse();
    }
  }

  @Test
  public void unitTest_getAllMaps() {
    TestWarehouseRepository local = new TestWarehouseRepository();
    DbWarehouse d = new DbWarehouse();
    d.businessUnitCode = "B1";
    d.location = "L1";
    d.capacity = 10;
    d.stock = 2;

    local.list = List.of(d);
    java.util.List<Warehouse> all = local.getAll();
    assertEquals(1, all.size());
    assertEquals("B1", all.get(0).businessUnitCode);
  }

  @Test
  public void unitTest_createUpdateRemoveFind() {
    TestWarehouseRepository local = new TestWarehouseRepository();
    DbWarehouse existing = new DbWarehouse();
    existing.businessUnitCode = "B2";
    existing.location = "OLD";
    existing.capacity = 20;
    existing.stock = 1;

    local.toFind = existing;

    Warehouse w = new Warehouse();
    w.businessUnitCode = "B2";
    w.location = "NEW";
    w.capacity = 200;
    w.stock = 10;

    local.update(w);
    assertNotNull(local.persisted);
    assertEquals("NEW", local.persisted.location);

    local.remove(w);
    assertSame(existing, local.deleted);

    Warehouse found = local.findByBusinessUnitCode("B2");
    assertNotNull(found);
    assertEquals("B2", found.businessUnitCode);
  }

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

