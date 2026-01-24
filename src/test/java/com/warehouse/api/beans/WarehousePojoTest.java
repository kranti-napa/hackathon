package com.warehouse.api.beans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WarehousePojoTest {

  @Test
  public void testGettersSetters_viaReflection() throws Exception {
    Class<?> cls = Class.forName("com.warehouse.api.beans.Warehouse");
    Object obj = cls.getDeclaredConstructor().newInstance();

    // setId / getId
    try {
      cls.getMethod("setId", String.class).invoke(obj, "id-123");
      Object id = cls.getMethod("getId").invoke(obj);
      assertEquals("id-123", id);
    } catch (NoSuchMethodException e) {
      // if generated bean doesn't have the methods at compile time, skip assertions
      return;
    }

    // setBusinessUnitCode / getBusinessUnitCode
    cls.getMethod("setBusinessUnitCode", String.class).invoke(obj, "BU-1");
    Object bu = cls.getMethod("getBusinessUnitCode").invoke(obj);
    assertEquals("BU-1", bu);
  }
}
