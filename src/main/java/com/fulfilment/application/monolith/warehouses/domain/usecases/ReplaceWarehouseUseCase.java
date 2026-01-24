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

	        Warehouse existing =
	            warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);

	        if (existing == null) {
	            throw new IllegalArgumentException(
	                "Warehouse not found for business unit: " + newWarehouse.businessUnitCode
	            );
	        }

	        // Stock must match previous warehouse
	        if (!existing.stock.equals(newWarehouse.stock)) {
	            throw new IllegalArgumentException(
	                "Stock must remain unchanged when replacing a warehouse"
	            );
	        }

	        if (newWarehouse.capacity == null || newWarehouse.capacity <= 0) {
	            throw new IllegalArgumentException("Capacity must be greater than zero");
	        }

	        warehouseStore.update(newWarehouse);
	    }


}
