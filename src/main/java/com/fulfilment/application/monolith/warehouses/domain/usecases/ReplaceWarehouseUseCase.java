package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.location.LocationGateway;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

	 private final WarehouseStore warehouseStore;
	 private final ArchiveWarehouseOperation archiveWarehouseOperation;
	 private final LocationGateway locationGateway;

	    public ReplaceWarehouseUseCase(
	            WarehouseStore warehouseStore,
	            ArchiveWarehouseOperation archiveWarehouseOperation,
	            LocationGateway locationGateway) {
	        this.warehouseStore = warehouseStore;
	        this.archiveWarehouseOperation = archiveWarehouseOperation;
	        this.locationGateway = locationGateway;
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

        // Validate new capacity is sufficient to hold the existing stock
        if (newWarehouse.capacity < existing.stock) {
            throw new ValidationException(AppConstants.ERR_WAREHOUSE_CAPACITY_INSUFFICIENT);
        }

        // Validate location capacity constraints if location or capacity changed
        if (!existing.location.equals(newWarehouse.location) || 
            !existing.capacity.equals(newWarehouse.capacity)) {
            
            Location location = locationGateway.resolveByIdentifier(newWarehouse.location);
            
            // Calculate total capacity: current location total - old capacity + new capacity
            int currentTotalCapacity = warehouseStore.getTotalCapacityByLocation(location.identification);
            int adjustedCapacity = currentTotalCapacity - existing.capacity + newWarehouse.capacity;
            
            if (adjustedCapacity > location.maxCapacity) {
                throw new ConflictException(AppConstants.ERR_WAREHOUSE_LOCATION_CAPACITY_EXCEEDED);
            }
        }

        // Archive the existing warehouse instead of overwriting it
        archiveWarehouseOperation.archive(existing);

        // Create new warehouse with same business unit code
        newWarehouse.createdAt = LocalDateTime.now();
        newWarehouse.archivedAt = null;
        warehouseStore.create(newWarehouse);
    }
}
