package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void create(Warehouse warehouse) {
	  if (warehouse == null) {
	        throw new IllegalArgumentException("Warehouse must not be null");
	    }

	    if (warehouseStore.existsByBusinessUnit(warehouse.businessUnit())) {
	        throw new IllegalArgumentException(
	                "Warehouse with business unit already exists: " + warehouse.businessUnit()
	        );
	    }

	    if (warehouse.capacity() <= 0) {
	        throw new IllegalArgumentException("Warehouse capacity must be greater than zero");
	    }

	    if (warehouse.stock() < 0) {
	        throw new IllegalArgumentException("Warehouse stock cannot be negative");
	    }
	    
	    if (warehouse.stock() > warehouse.capacity()) {
	        throw new IllegalArgumentException("Warehouse stock cannot exceed capacity");
	    }


    // if all went well, create the warehouse
    warehouseStore.create(warehouse);
  }
}
