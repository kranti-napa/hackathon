package com.fulfilment.application.monolith.warehouses.domain.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class LocationPojoExtraTest {

  @Test
  public void gettersSetters_work() {
    Location l = new Location("LOC-1", 3, 100);
    assertEquals("LOC-1", l.getIdentification());
    assertEquals(3, l.getMaxNumberOfWarehouses());
    assertEquals(100, l.getMaxCapacity());

    l.setIdentification("LOC-2");
    l.setMaxNumberOfWarehouses(5);
    l.setMaxCapacity(200);

    assertEquals("LOC-2", l.getIdentification());
    assertEquals(5, l.getMaxNumberOfWarehouses());
    assertEquals(200, l.getMaxCapacity());
  }
}
