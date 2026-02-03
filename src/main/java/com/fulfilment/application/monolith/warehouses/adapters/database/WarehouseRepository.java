package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
@Transactional
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOGGER = Logger.getLogger(WarehouseRepository.class);
  private static final String FIELD_BUSINESS_UNIT_CODE = "businessUnitCode";
  private static final String LOG_CREATE_NULL = "create called with null warehouse";
  private static final String LOG_CREATE_SUCCESS = "Created warehouse db record for bu=%s";
  private static final String LOG_UPDATE_NULL = "update called with null warehouse";
  private static final String LOG_UPDATE_NOT_FOUND = "Warehouse not found for update, bu=%s";
  private static final String LOG_UPDATE_SUCCESS = "Updated warehouse db record for bu=%s";
  private static final String LOG_REMOVE_NULL = "remove called with null warehouse";
  private static final String LOG_REMOVE_SUCCESS = "Deleted warehouse db record for bu=%s";
  private static final String LOG_REMOVE_NOTHING = "remove: nothing to delete for bu=%s";
  private static final String LOG_FIND_NULL = "findByBusinessUnitCode called with null buCode";
  private static final String LOG_FIND_NOTHING = "No warehouse found for bu=%s";

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      LOGGER.warn(LOG_CREATE_NULL);
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
    LOGGER.debugf(LOG_CREATE_SUCCESS, warehouse.businessUnitCode);
  }

  @Override
  public void update(Warehouse warehouse) {
    if (warehouse == null) {
      LOGGER.warn(LOG_UPDATE_NULL);
      throw new ValidationException(AppConstants.ERR_WAREHOUSE_NULL);
    }

    DbWarehouse existing = find(FIELD_BUSINESS_UNIT_CODE, warehouse.businessUnitCode).firstResult();

    if (existing == null) {
      LOGGER.warnf(LOG_UPDATE_NOT_FOUND, warehouse.businessUnitCode);
      throw new NotFoundException(String.format(AppConstants.ERR_WAREHOUSE_NOT_FOUND, warehouse.businessUnitCode));
    }

    existing.location = warehouse.location;
    existing.capacity = warehouse.capacity;
    existing.stock = warehouse.stock;
    existing.archivedAt = warehouse.archivedAt;

    persist(existing);
    LOGGER.debugf(LOG_UPDATE_SUCCESS, warehouse.businessUnitCode);
  }

  @Override
  public void remove(Warehouse warehouse) {
    if (warehouse == null) {
      LOGGER.warn(LOG_REMOVE_NULL);
      return;
    }

    DbWarehouse existing = find(FIELD_BUSINESS_UNIT_CODE, warehouse.businessUnitCode).firstResult();

    if (existing != null) {
      delete(existing);
      LOGGER.debugf(LOG_REMOVE_SUCCESS, warehouse.businessUnitCode);
    } else {
      LOGGER.debugf(LOG_REMOVE_NOTHING, warehouse.businessUnitCode);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null) {
      LOGGER.debug(LOG_FIND_NULL);
      return null;
    }

    // Find only active (non-archived) warehouse
    DbWarehouse existing = find(FIELD_BUSINESS_UNIT_CODE + " = ?1 and archivedAt is null", buCode).firstResult();
    if (existing == null) {
      LOGGER.debugf(LOG_FIND_NOTHING, buCode);
      return null;
    }
    return existing.toWarehouse();
  }

  @Override
  public long countByLocation(String location) {
    if (location == null) {
      return 0;
    }
    // Use count query for efficiency instead of loading all records
    return count("location = ?1 and archivedAt is null", location);
  }

  @Override
  public int getTotalCapacityByLocation(String location) {
    if (location == null) {
      return 0;
    }
    // Sum capacity of all active warehouses at this location
    Long totalCapacity = find("location = ?1 and archivedAt is null", location)
        .stream()
        .mapToLong(w -> w.capacity != null ? w.capacity : 0)
        .sum();
    return totalCapacity.intValue();
  }
}
