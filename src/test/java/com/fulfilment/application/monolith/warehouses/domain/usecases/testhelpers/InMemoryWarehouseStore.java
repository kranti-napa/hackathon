package com.fulfilment.application.monolith.warehouses.domain.usecases.testhelpers;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryWarehouseStore implements WarehouseStore {

  private final List<Warehouse> list = new CopyOnWriteArrayList<>();

  @Override
  public List<Warehouse> getAll() {
    return new ArrayList<>(list);
  }

  @Override
  public void create(Warehouse warehouse) {
    list.add(warehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    for (int i = 0; i < list.size(); i++) {
      Warehouse w = list.get(i);
      if (w.businessUnitCode != null && w.businessUnitCode.equals(warehouse.businessUnitCode)) {
        // Update: preserve the old warehouse, find the first unarchived one
        if (w.archivedAt == null) {
          list.set(i, warehouse);
          return;
        }
      }
    }
    // if not found, add
    list.add(warehouse);
  }

  @Override
  public void remove(Warehouse warehouse) {
    list.removeIf(w -> w.businessUnitCode != null && w.businessUnitCode.equals(warehouse.businessUnitCode));
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    // Find the first active (not archived) warehouse with matching business unit code
    return list.stream()
        .filter(w -> w.businessUnitCode != null && w.businessUnitCode.equals(buCode) && w.archivedAt == null)
        .findFirst()
        .orElse(null);
  }

  @Override
  public long countByLocation(String location) {
    if (location == null) {
      return 0;
    }
    return list.stream()
        .filter(w -> w.location != null && w.location.equals(location) && w.archivedAt == null)
        .count();
  }

  @Override
  public int getTotalCapacityByLocation(String location) {
    if (location == null) {
      return 0;
    }
    return list.stream()
        .filter(w -> w.location != null && w.location.equals(location) && w.archivedAt == null)
        .mapToInt(w -> w.capacity != null ? w.capacity : 0)
        .sum();
  }
}
