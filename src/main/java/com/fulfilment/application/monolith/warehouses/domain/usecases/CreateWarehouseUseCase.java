package com.fulfilment.application.monolith.warehouses.domain.usecases;

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

        // 1. Business Unit Code Verification
        Warehouse existing =
                warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);

        if (existing != null) {
            throw new IllegalArgumentException(
                    "Warehouse with business unit code already exists");
        }

        // 2. Location Validation
        Location location =
                locationGateway.resolveByIdentifier(warehouse.location);

        // 3. Warehouse Creation Feasibility
        long warehousesInLocation = warehouseStore.getAll().stream()
                .filter(w -> w.location.equals(location.identification))
                .count();

        if (warehousesInLocation >= location.maxNumberOfWarehouses) {
            throw new IllegalStateException(
                    "Maximum number of warehouses reached for location");
        }

        // 4. Capacity Validation
        if (warehouse.capacity == null || warehouse.capacity <= 0) {
            throw new IllegalArgumentException("Invalid warehouse capacity");
        }

        // 5. Stock Validation
        if (warehouse.stock == null || warehouse.stock < 0) {
            throw new IllegalArgumentException("Invalid warehouse stock");
        }

        warehouse.createdAt = LocalDateTime.now();
        warehouse.archivedAt = null;

        warehouseStore.create(warehouse);
    }
}
