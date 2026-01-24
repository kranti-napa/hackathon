package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
	  DbWarehouse dbWarehouse = DbWarehouse.fromWarehouse(warehouse);
      persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
	  DbWarehouse existing =
              find("businessUnitCode", warehouse.businessUnit()).firstResult();

      if (existing == null) {
          throw new IllegalArgumentException(
                  "Warehouse not found with business unit: " + warehouse.businessUnit());
      }

      existing.updateFromDomain(warehouse);
      persist(existing);
  }

  @Override
  public void remove(Warehouse warehouse) {
	  DbWarehouse existing =
              find("businessUnitCode", warehouse.businessUnit()).firstResult();

      if (existing != null) {
          delete(existing);
      }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'findById'");
  }
}
