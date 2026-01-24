package com.fulfilment.application.monolith.products;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProductModelTest {

  @Test
  public void testConstructors() {
    Product p = new Product();
    Assertions.assertNull(p.name);

    Product p2 = new Product("nm");
    Assertions.assertEquals("nm", p2.name);
  }
}
