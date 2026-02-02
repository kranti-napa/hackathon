package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.location.LocationGateway;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

    private final WarehouseStore warehouseStore;
    private final LocationGateway locationGateway;

    public CreateWarehouseUseCase(
            WarehouseStore warehouseStore,
            LocationGateway locationGateway) {
        this.warehouseStore = warehouseStore;
        this.locationGateway = locationGateway;
    }

    @Override
    public void create(Warehouse warehouse) {

        if (warehouse == null) {
            throw new ValidationException(AppConstants.ERR_WAREHOUSE_NULL);
        }

        // 1. Business Unit Code Verification
        Warehouse existing =
                warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);

        if (existing != null) {
            throw new ConflictException(AppConstants.ERR_WAREHOUSE_ALREADY_EXISTS);
        }

        // 2. Location Validation
        Location location =
                locationGateway.resolveByIdentifier(warehouse.location);

        // 3. Warehouse Creation Feasibility
        long warehousesInLocation = warehouseStore.countByLocation(location.identification);

        if (warehousesInLocation >= location.maxNumberOfWarehouses) {
            throw new ConflictException(AppConstants.ERR_WAREHOUSE_MAX_PER_LOCATION);
        }

        // 4. Capacity Validation
        if (warehouse.capacity == null || warehouse.capacity <= 0) {
            throw new ValidationException(AppConstants.ERR_WAREHOUSE_INVALID_CAPACITY);
        }

        // 5. Stock Validation
        if (warehouse.stock == null || warehouse.stock < 0) {
            throw new ValidationException(AppConstants.ERR_WAREHOUSE_INVALID_STOCK);
        }

        warehouse.createdAt = LocalDateTime.now();
        warehouse.archivedAt = null;

        warehouseStore.create(warehouse);
    }
}
