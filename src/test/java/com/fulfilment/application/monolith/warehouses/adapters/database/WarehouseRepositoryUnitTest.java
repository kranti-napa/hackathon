package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.List;
import org.junit.jupiter.api.Test;

public class WarehouseRepositoryUnitTest {

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
        throw new IllegalArgumentException("not found");
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
  public void testGetAllMaps() {
    TestWarehouseRepository repo = new TestWarehouseRepository();
    DbWarehouse d = new DbWarehouse();
    d.businessUnitCode = "B1";
    d.location = "L1";
    d.capacity = 10;
    d.stock = 2;

    repo.list = List.of(d);
    java.util.List<Warehouse> all = repo.getAll();
    assertEquals(1, all.size());
    assertEquals("B1", all.get(0).businessUnitCode);
  }

  @Test
  public void testCreatePersistsDb() {
    TestWarehouseRepository repo = new TestWarehouseRepository();
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BUX";
    w.location = "LOC";
    w.capacity = 55;
    w.stock = 5;

    repo.create(w);
    assertNotNull(repo.persisted);
    assertEquals("BUX", repo.persisted.businessUnitCode);
  }

  @Test
  public void testUpdateAndRemoveAndFind() {
    TestWarehouseRepository repo = new TestWarehouseRepository();
    DbWarehouse existing = new DbWarehouse();
    existing.businessUnitCode = "B2";
    existing.location = "OLD";
    existing.capacity = 20;
    existing.stock = 1;

    repo.toFind = existing;

    Warehouse w = new Warehouse();
    w.businessUnitCode = "B2";
    w.location = "NEW";
    w.capacity = 200;
    w.stock = 10;

    repo.update(w);
    assertNotNull(repo.persisted);
    assertEquals("NEW", repo.persisted.location);

    repo.remove(w);
    assertSame(existing, repo.deleted);

    // findByBusinessUnitCode
    repo.toFind = existing;
    Warehouse found = repo.findByBusinessUnitCode("B2");
    assertNotNull(found);
    assertEquals("B2", found.businessUnitCode);
  }
}
