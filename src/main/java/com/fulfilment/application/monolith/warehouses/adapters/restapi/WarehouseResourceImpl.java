package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
	  warehouseRepository.persist(data);
      return toWarehouseResponse(data);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
	  Warehouse warehouse = warehouseRepository.findById(id);
      if (warehouse == null) {
          throw new IllegalArgumentException("Warehouse not found with id: " + id);
      }
      return toWarehouseResponse(warehouse);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
	  Warehouse warehouse = warehouseRepository.findById(id);
      if (warehouse == null) {
          throw new IllegalArgumentException("Warehouse not found with id: " + id);
      }

      warehouse.archive();
      warehouseRepository.update(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
	  Warehouse existingWarehouse =
	            warehouseRepository.findByBusinessUnitCode(businessUnitCode);

	    if (existingWarehouse == null) {
	        throw new IllegalArgumentException(
	                "Warehouse not found with business unit code: " + businessUnitCode);
	    }

	    // Update fields from input
	    existingWarehouse.setLocation(data.getLocation());
	    existingWarehouse.setCapacity(data.getCapacity());
	    existingWarehouse.setStock(data.getStock());

	    warehouseRepository.update(existingWarehouse);

	    return toWarehouseResponse(existingWarehouse);
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
