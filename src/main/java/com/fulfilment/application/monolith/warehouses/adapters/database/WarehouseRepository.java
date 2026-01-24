package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOGGER = Logger.getLogger(WarehouseRepository.class);

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      LOGGER.warn("create called with null warehouse");
      return;
    }

    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = warehouse.businessUnitCode;
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.createdAt = warehouse.createdAt;
    db.archivedAt = warehouse.archivedAt;

    persist(db);
    LOGGER.debugf("Created warehouse db record for bu=%s", warehouse.businessUnitCode);
  }

  @Override
  public void update(Warehouse warehouse) {
    if (warehouse == null) {
      LOGGER.warn("update called with null warehouse");
      throw new IllegalArgumentException("Warehouse must not be null");
    }

    DbWarehouse existing = find("businessUnitCode", warehouse.businessUnitCode).firstResult();

    if (existing == null) {
      LOGGER.warnf("Warehouse not found for update, bu=%s", warehouse.businessUnitCode);
      throw new IllegalArgumentException("Warehouse not found with business unit: " + warehouse.businessUnitCode);
    }

    existing.location = warehouse.location;
    existing.capacity = warehouse.capacity;
    existing.stock = warehouse.stock;
    existing.archivedAt = warehouse.archivedAt;

    persist(existing);
    LOGGER.debugf("Updated warehouse db record for bu=%s", warehouse.businessUnitCode);
  }

  @Override
  public void remove(Warehouse warehouse) {
    if (warehouse == null) {
      LOGGER.warn("remove called with null warehouse");
      return;
    }

    DbWarehouse existing = find("businessUnitCode", warehouse.businessUnitCode).firstResult();

    if (existing != null) {
      delete(existing);
      LOGGER.debugf("Deleted warehouse db record for bu=%s", warehouse.businessUnitCode);
    } else {
      LOGGER.debugf("remove: nothing to delete for bu=%s", warehouse.businessUnitCode);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null) {
      LOGGER.debug("findByBusinessUnitCode called with null buCode");
      return null;
    }

    DbWarehouse existing = find("businessUnitCode", buCode).firstResult();
    if (existing == null) {
      LOGGER.debugf("No warehouse found for bu=%s", buCode);
      return null;
    }
    return existing.toWarehouse();
  }
}
