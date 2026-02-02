package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

	 private final WarehouseStore warehouseStore;
	 private final ArchiveWarehouseOperation archiveWarehouseOperation;

	    public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, ArchiveWarehouseOperation archiveWarehouseOperation) {
	        this.warehouseStore = warehouseStore;
	        this.archiveWarehouseOperation = archiveWarehouseOperation;
	    }

	    @Override
	    public void replace(Warehouse newWarehouse) {

			if (newWarehouse == null || newWarehouse.businessUnitCode == null) {
				throw new ValidationException(AppConstants.ERR_WAREHOUSE_NULL);
			}

	        Warehouse existing =
	            warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);

	        if (existing == null) {
	            throw new NotFoundException(
	                String.format(AppConstants.ERR_WAREHOUSE_NOT_FOUND, newWarehouse.businessUnitCode)
	            );
	        }

	        // Stock must match previous warehouse
	        if (!existing.stock.equals(newWarehouse.stock)) {
	            throw new ConflictException(AppConstants.ERR_WAREHOUSE_STOCK_IMMUTABLE);
	        }

	        if (newWarehouse.capacity == null || newWarehouse.capacity <= 0) {
	            throw new ValidationException(AppConstants.ERR_WAREHOUSE_CAPACITY_REQUIRED);
	        }

	        // Validate capacity is sufficient for stock
	        if (newWarehouse.capacity < newWarehouse.stock) {
	            throw new ValidationException(AppConstants.ERR_WAREHOUSE_CAPACITY_INSUFFICIENT);
	        }

	        // Archive the existing warehouse instead of overwriting it
	        archiveWarehouseOperation.archive(existing);

	        // Create new warehouse with same business unit code
	        newWarehouse.createdAt = LocalDateTime.now();
	        newWarehouse.archivedAt = null;
	        warehouseStore.create(newWarehouse);
	    }


}
