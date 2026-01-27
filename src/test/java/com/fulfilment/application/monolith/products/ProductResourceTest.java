package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
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
      if (entity.id == null) {
        entity.id = 1L;
      }
    }

    public void delete(Product entity) {
      this.deleted = entity;
    }
  }

  // ---------- ENDPOINT TESTS ----------

  @Test
  public void testCrudProductEndpoint() {
    final String path = "product";

    String name = "P-" + System.currentTimeMillis();
    Product p = new Product(name);
    p.stock = 1;

    Long id =
      given()
        .contentType(ContentType.JSON)
        .body(p)
      .when()
        .post(path)
      .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    given()
      .when()
      .get(path)
      .then()
      .statusCode(200)
      .body(containsString(name));

    given().when().delete(path + "/" + id).then().statusCode(204);

    given()
      .when()
      .get(path)
      .then()
      .statusCode(200)
      .body(not(containsString(name)));
  }

  // ---------- RESOURCE TESTS ----------

  @Test
  public void testGetReturnsList() {
    Product p = new Product("p1");
    TestProductRepository repo = new TestProductRepository();
    repo.list = List.of(p);

    ProductResource r = new ProductResource();
    injectRepo(r, repo);

    var got = r.get();
    Assertions.assertEquals(1, got.size());
    Assertions.assertSame(p, got.get(0));
  }

  @Test
  public void testGetSingleNotFoundThrows() {
    TestProductRepository repo = new TestProductRepository();
    repo.found = null;
    ProductResource r = new ProductResource();
    injectRepo(r, repo);

    Assertions.assertThrows(NotFoundException.class, () -> r.getSingle(99L));
  }

  @Test
  public void testCreateRejectsWhenIdSet() {
    TestProductRepository repo = new TestProductRepository();
    ProductResource r = new ProductResource();
    injectRepo(r, repo);

    Product p = new Product("x");
    p.id = 123L;
    Assertions.assertThrows(ConflictException.class, () -> r.create(p));
  }

  @Test
  public void testCreatePersists() {
    TestProductRepository repo = new TestProductRepository();
    ProductResource r = new ProductResource();
    injectRepo(r, repo);

    Product p = new Product("x");
    Response resp = r.create(p);
    Assertions.assertEquals(201, resp.getStatus());
    Assertions.assertNotNull(p.id);
  }

  @Test
  public void testUpdateValidationNotFoundAndSuccess() {
    TestProductRepository repo = new TestProductRepository();
    ProductResource r = new ProductResource();
    injectRepo(r, repo);

    Product p = new Product();
    Assertions.assertThrows(ValidationException.class, () -> r.update(1L, p));

    Product upd = new Product("n");
    upd.description = "d";
    upd.price = new BigDecimal("1.23");
    upd.stock = 5;
    repo.found = null;
    Assertions.assertThrows(NotFoundException.class, () -> r.update(2L, upd));

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
    injectRepo(r, repo);

    repo.found = null;
    Assertions.assertThrows(NotFoundException.class, () -> r.delete(99L));

    Product existing = new Product("todel");
    existing.id = 7L;
    repo.found = existing;
    Response resp = r.delete(7L);
    Assertions.assertEquals(204, resp.getStatus());
    Assertions.assertSame(existing, repo.deleted);
  }

  // ---------- ERROR MAPPER TESTS ----------

  @Test
  public void testErrorMapperHandlesCustomExceptions() throws Exception {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    ObjectMapper om = new ObjectMapper();
    java.lang.reflect.Field f = ProductResource.ErrorMapper.class.getDeclaredField("objectMapper");
    f.setAccessible(true);
    f.set(mapper, om);

    Response r = mapper.toResponse(new NotFoundException(String.format(AppConstants.ERR_PRODUCT_NOT_FOUND, 1)));
    Assertions.assertEquals(404, r.getStatus());

    Response r2 = mapper.toResponse(new ValidationException(AppConstants.ERR_PRODUCT_NAME_REQUIRED));
    Assertions.assertEquals(400, r2.getStatus());

    Response r3 = mapper.toResponse(new ConflictException(AppConstants.ERR_PRODUCT_ID_NOT_ALLOWED));
    Assertions.assertEquals(409, r3.getStatus());

    Object entity = r.getEntity();
    ObjectNode node;
    if (entity instanceof ObjectNode) {
      node = (ObjectNode) entity;
    } else if (entity instanceof byte[]) {
      node = (ObjectNode) om.readTree((byte[]) entity);
    } else {
      node = (ObjectNode) om.readTree(entity.toString());
    }

    Assertions.assertTrue(node.has("code"));
  }

  private void injectRepo(ProductResource resource, ProductRepository repo) {
    try {
      java.lang.reflect.Field f = ProductResource.class.getDeclaredField("productRepository");
      f.setAccessible(true);
      f.set(resource, repo);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
