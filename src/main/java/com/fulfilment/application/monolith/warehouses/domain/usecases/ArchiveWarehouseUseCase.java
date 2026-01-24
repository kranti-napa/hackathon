package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;


@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {

      if (warehouse == null || warehouse.businessUnitCode == null) {
          throw new IllegalArgumentException("Invalid warehouse for archiving");
      }

      Warehouse existing =
          warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);

      if (existing == null) {
          throw new IllegalArgumentException(
              "Warehouse not found for business unit: " + warehouse.businessUnitCode
          );
      }

      existing.archivedAt = LocalDateTime.now();
      warehouseStore.update(existing);
  }
}
