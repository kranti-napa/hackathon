package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    
	  if (newWarehouse == null) {
	        throw new IllegalArgumentException("Warehouse must not be null");
	    }

	    if (!warehouseStore.existsByBusinessUnit(newWarehouse.businessUnit())) {
	        throw new IllegalArgumentException(
	                "Cannot replace non-existing warehouse with business unit: "
	                        + newWarehouse.businessUnit()
	        );
	    }

	    if (newWarehouse.capacity() <= 0) {
	        throw new IllegalArgumentException("Warehouse capacity must be greater than zero");
	    }

	    if (newWarehouse.stock() < 0) {
	        throw new IllegalArgumentException("Warehouse stock cannot be negative");
	    }

	    if (newWarehouse.stock() > newWarehouse.capacity()) {
	        throw new IllegalArgumentException("Warehouse stock cannot exceed capacity");
	    }

    warehouseStore.update(newWarehouse);
  }
}
