package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;



@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

    @Inject
    WarehouseRepository warehouseRepository;

    private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class);

    @Override
    public List<com.warehouse.api.beans.Warehouse> listAllWarehousesUnits() {

        List<com.warehouse.api.beans.Warehouse> response = new ArrayList<>();

        List<com.fulfilment.application.monolith.warehouses.domain.models.Warehouse> domains =
                warehouseRepository.getAll();

        for (com.fulfilment.application.monolith.warehouses.domain.models.Warehouse d : domains) {
            response.add(toApi(d));
        }

        return response;
    }

    @Override
    public com.warehouse.api.beans.Warehouse getAWarehouseUnitByID(String businessUnitCode) {

        LOGGER.debugf("getAWarehouseUnitByID called bu=%s", businessUnitCode);

        for (com.fulfilment.application.monolith.warehouses.domain.models.Warehouse d
                : warehouseRepository.getAll()) {

            if (businessUnitCode.equals(d.businessUnitCode)) {
                return toApi(d);
            }
        }

    throw new IllegalArgumentException("Warehouse not found with businessUnitCode: " + businessUnitCode);

    }

    @Override
    public com.warehouse.api.beans.Warehouse createANewWarehouseUnit(
            @NotNull com.warehouse.api.beans.Warehouse api) {

        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse d =
                toDomain(api);

        d.createdAt = LocalDateTime.now();
        warehouseRepository.create(d);
    LOGGER.debugf("createANewWarehouseUnit created bu=%s", api == null ? "<null>" : api.getBusinessUnitCode());
    return api;
    }

    @Override
    public com.warehouse.api.beans.Warehouse replaceTheCurrentActiveWarehouse(
            String businessUnitCode,
            @NotNull com.warehouse.api.beans.Warehouse api) {

        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse d =
                toDomain(api);

        d.businessUnitCode = businessUnitCode;
        warehouseRepository.update(d);
    LOGGER.debugf("replaceTheCurrentActiveWarehouse for bu=%s", businessUnitCode);
    return api;
    }

    @Override
    public void archiveAWarehouseUnitByID(String businessUnitCode) {

        LOGGER.debugf("archiveAWarehouseUnitByID called bu=%s", businessUnitCode);

        for (com.fulfilment.application.monolith.warehouses.domain.models.Warehouse d
                : warehouseRepository.getAll()) {

            if (businessUnitCode.equals(d.businessUnitCode)) {
                d.archivedAt = java.time.LocalDateTime.now();
                warehouseRepository.update(d);
                return;
            }
        }

    throw new IllegalArgumentException("Warehouse not found with businessUnitCode: " + businessUnitCode);
    }

    // ---------- MAPPERS ----------

    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(
            com.warehouse.api.beans.Warehouse api) {

        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse d =
                new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

        d.businessUnitCode = api.getBusinessUnitCode();
        d.location = api.getLocation();
        d.capacity = api.getCapacity();
        d.stock = api.getStock();

        return d;
    }

    private com.warehouse.api.beans.Warehouse toApi(
            com.fulfilment.application.monolith.warehouses.domain.models.Warehouse d) {

        com.warehouse.api.beans.Warehouse api =
                new com.warehouse.api.beans.Warehouse();

        api.setBusinessUnitCode(d.businessUnitCode);
        api.setLocation(d.location);
        api.setCapacity(d.capacity);
        api.setStock(d.stock);

        return api;
    }
}
