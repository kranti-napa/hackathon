package com.fulfilment.application.monolith.fulfilment.domain.usecases;

import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentAssignmentStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AssignWarehouseToStoreProductUseCase {

    private final FulfilmentAssignmentStore assignmentStore;

    public AssignWarehouseToStoreProductUseCase(
            FulfilmentAssignmentStore assignmentStore) {
        this.assignmentStore = assignmentStore;
    }

    public void assign(String storeId,
                       String productId,
                       String warehouseBusinessUnitCode) {

        List<FulfilmentAssignment> all = assignmentStore.getAll();

        // Constraint 1:
        // Each Product → max 2 Warehouses per Store
        long warehousesForProductInStore =
            all.stream()
               .filter(a -> a.storeId.equals(storeId))
               .filter(a -> a.productId.equals(productId))
               .count();

        if (warehousesForProductInStore >= 2) {
            throw new IllegalStateException(
                "Product can be fulfilled by max 2 warehouses per store");
        }

        // Constraint 2:
        // Each Store → max 3 Warehouses
        long warehousesForStore =
            all.stream()
               .filter(a -> a.storeId.equals(storeId))
               .map(a -> a.warehouseBusinessUnitCode)
               .distinct()
               .count();

        if (warehousesForStore >= 3) {
            throw new IllegalStateException(
                "Store can be fulfilled by max 3 warehouses");
        }

        // Constraint 3:
        // Each Warehouse → max 5 Products
        long productsInWarehouse =
            all.stream()
               .filter(a -> a.warehouseBusinessUnitCode
                               .equals(warehouseBusinessUnitCode))
               .map(a -> a.productId)
               .distinct()
               .count();

        if (productsInWarehouse >= 5) {
            throw new IllegalStateException(
                "Warehouse can store max 5 products");
        }

        assignmentStore.create(
            new FulfilmentAssignment(
                storeId,
                productId,
                warehouseBusinessUnitCode
            )
        );
    }
}
