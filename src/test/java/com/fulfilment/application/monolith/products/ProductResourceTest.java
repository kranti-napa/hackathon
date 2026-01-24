package com.fulfilment.application.monolith.products;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProductResourceTest {

  static class TestProductRepository extends ProductRepository {
    List<Product> list;
    Product found;
    Product deleted;

    @Override
    public java.util.List<Product> listAll(io.quarkus.panache.common.Sort sort) {
      return list;
    }

    @Override
    public Product findById(Long id) {
      return found;
    }

    @Override
    public void persist(Product entity) {
      // mimic persistence assigning an id if missing
      if (entity.id == null) {
        entity.id = 1L;
      }
    }

    public void delete(Product entity) {
      this.deleted = entity;
    }
  }

  @Test
  public void testGetReturnsList() {
    Product p = new Product("p1");
    TestProductRepository repo = new TestProductRepository();
    repo.list = List.of(p);

    ProductResource r = new ProductResource();
    // inject test repo
    try {
      java.lang.reflect.Field f = ProductResource.class.getDeclaredField("productRepository");
      f.setAccessible(true);
      f.set(r, repo);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    var got = r.get();
    Assertions.assertEquals(1, got.size());
    Assertions.assertSame(p, got.get(0));
  }

  @Test
  public void testGetSingleNotFoundThrows() {
    TestProductRepository repo = new TestProductRepository();
    repo.found = null;
    ProductResource r = new ProductResource();
    try {
      java.lang.reflect.Field f = ProductResource.class.getDeclaredField("productRepository");
      f.setAccessible(true);
      f.set(r, repo);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class, () -> r.getSingle(99L));
    Assertions.assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  public void testCreateRejectsWhenIdSet() {
    TestProductRepository repo = new TestProductRepository();
    ProductResource r = new ProductResource();
    try {
      java.lang.reflect.Field f = ProductResource.class.getDeclaredField("productRepository");
      f.setAccessible(true);
      f.set(r, repo);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    Product p = new Product("x");
    p.id = 123L;
    WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class, () -> r.create(p));
    Assertions.assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  public void testCreatePersists() {
    TestProductRepository repo = new TestProductRepository();
    ProductResource r = new ProductResource();
    try {
      java.lang.reflect.Field f = ProductResource.class.getDeclaredField("productRepository");
      f.setAccessible(true);
      f.set(r, repo);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    Product p = new Product("x");
    Response resp = r.create(p);
    Assertions.assertEquals(201, resp.getStatus());
    Assertions.assertNotNull(p.id);
  }

  @Test
  public void testUpdateValidationAndNotFoundAndSuccess() {
    TestProductRepository repo = new TestProductRepository();
    ProductResource r = new ProductResource();
    try {
      java.lang.reflect.Field f = ProductResource.class.getDeclaredField("productRepository");
      f.setAccessible(true);
      f.set(r, repo);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // missing name
    Product p = new Product();
    WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class, () -> r.update(1L, p));
    Assertions.assertEquals(422, ex.getResponse().getStatus());

    // not found
    Product upd = new Product("n");
    upd.description = "d";
    upd.price = new BigDecimal("1.23");
    upd.stock = 5;
    repo.found = null;
    ex = Assertions.assertThrows(WebApplicationException.class, () -> r.update(2L, upd));
    Assertions.assertEquals(404, ex.getResponse().getStatus());

    // success
    Product existing = new Product("old");
    existing.id = 2L;
    repo.found = existing;
    Product res = r.update(2L, upd);
    Assertions.assertEquals("n", res.name);
    Assertions.assertEquals("d", res.description);
  }

  @Test
  public void testDeleteNotFoundAndSuccess() {
    TestProductRepository repo = new TestProductRepository();
    ProductResource r = new ProductResource();
    try {
      java.lang.reflect.Field f = ProductResource.class.getDeclaredField("productRepository");
      f.setAccessible(true);
      f.set(r, repo);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    repo.found = null;
    WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class, () -> r.delete(99L));
    Assertions.assertEquals(404, ex.getResponse().getStatus());

    Product existing = new Product("todel");
    existing.id = 7L;
    repo.found = existing;
    Response resp = r.delete(7L);
    Assertions.assertEquals(204, resp.getStatus());
    Assertions.assertSame(existing, repo.deleted);
  }
}
