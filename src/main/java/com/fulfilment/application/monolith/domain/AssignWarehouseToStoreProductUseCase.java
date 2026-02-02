package com.fulfilment.application.monolith.fulfilment.domain.usecases;

import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentAssignmentStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        boolean alreadyAssigned =
            all.stream().anyMatch(a ->
                a.storeId.equals(storeId)
                    && a.productId.equals(productId)
                    && a.warehouseBusinessUnitCode.equals(warehouseBusinessUnitCode));

        if (alreadyAssigned) {
            return;
        }

        // Optimize: Single pass through all assignments to collect counts
        long warehousesForProductInStore = 0;
        long warehousesForStore = 0;
        long productsInWarehouse = 0;
        
        Set<String> warehousesInProduct = new HashSet<>();
        Set<String> warehousesInStore = new HashSet<>();
        Set<String> productsInWarehouseSet = new HashSet<>();

        for (FulfilmentAssignment a : all) {
            // Constraint 1: Each Product → max 2 Warehouses per Store
            if (a.storeId.equals(storeId) && a.productId.equals(productId)) {
                warehousesInProduct.add(a.warehouseBusinessUnitCode);
            }

            // Constraint 2: Each Store → max 3 Warehouses
            if (a.storeId.equals(storeId)) {
                warehousesInStore.add(a.warehouseBusinessUnitCode);
            }

            // Constraint 3: Each Warehouse → max 5 Products
            if (a.warehouseBusinessUnitCode.equals(warehouseBusinessUnitCode)) {
                productsInWarehouseSet.add(a.productId);
            }
        }

        warehousesForProductInStore = warehousesInProduct.size();
        warehousesForStore = warehousesInStore.size();
        productsInWarehouse = productsInWarehouseSet.size();

        // Constraint 1 check
        if (warehousesForProductInStore >= 2) {
            throw new ConflictException(
                AppConstants.ERR_ASSIGN_MAX_WAREHOUSES_PER_PRODUCT);
        }

        // Constraint 2 check
        if (warehousesForStore >= 3) {
            throw new ConflictException(
                AppConstants.ERR_ASSIGN_MAX_WAREHOUSES_PER_STORE);
        }

        // Constraint 3 check
        if (productsInWarehouse >= 5) {
                warehouseBusinessUnitCode
            )
        );
    }
}
