package com.fulfilment.application.monolith.products;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProductModelTest {

  @Test
  public void testConstructors() {
    Product p = new Product();
    Assertions.assertNull(p.name);

    Product p2 = new Product("nm");
    Assertions.assertEquals("nm", p2.name);
  }

  @Test
  public void testFieldAssignments() {
    Product product = new Product("Test Product");
    product.description = "A test description";
    product.price = new BigDecimal("19.99");
    product.stock = 100;

    assertEquals("Test Product", product.name);
    assertEquals("A test description", product.description);
    assertEquals(new BigDecimal("19.99"), product.price);
    assertEquals(100, product.stock);
  }

  @Test
  public void testNullableFields() {
    Product product = new Product("Only Name");
    assertNull(product.description);
    assertNull(product.price);
    assertEquals(0, product.stock); // primitive int defaults to 0
  }

  @Test
  public void testZeroStock() {
    Product product = new Product("Zero Stock");
    product.stock = 0;
    assertEquals(0, product.stock);
  }

  @Test
  public void testNegativeStock() {
    Product product = new Product("Negative Stock");
    product.stock = -10;
    assertEquals(-10, product.stock);
  }

  @Test
  public void testPriceWithDecimals() {
    Product product = new Product("Decimal Price");
    product.price = new BigDecimal("123.456789");
    assertEquals(new BigDecimal("123.456789"), product.price);
  }

  @Test
  public void testEmptyName() {
    Product product = new Product("");
    assertEquals("", product.name);
  }

  @Test
  public void testLongDescription() {
    Product product = new Product("Long Desc");
    String longDesc = "A".repeat(1000);
    product.description = longDesc;
    assertEquals(longDesc, product.description);
  }

  @Test
  public void testZeroPrice() {
    Product product = new Product("Free Product");
    product.price = BigDecimal.ZERO;
    assertEquals(BigDecimal.ZERO, product.price);
  }

  @Test
  public void testProductIdAssignment() {
    Product product = new Product("ID Test");
    assertNull(product.id);
    product.id = 100L;
    assertEquals(100L, product.id);
  }

  @Test
  public void testMultipleFieldAssignments() {
    Product product = new Product();
    product.name = "Complete Product";
    product.description = "Full description";
    product.price = new BigDecimal("49.99");
    product.stock = 200;
    product.id = 1L;

    assertEquals("Complete Product", product.name);
    assertEquals("Full description", product.description);
    assertEquals(new BigDecimal("49.99"), product.price);
    assertEquals(200, product.stock);
    assertEquals(1L, product.id);
  }
}
