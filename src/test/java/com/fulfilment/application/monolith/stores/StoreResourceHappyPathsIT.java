package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreResourceHappyPathsIT {

  @Inject
  StoreResource storeResource;

  @Test
  public void testGet_returnsSomeStores() {
    List<Store> all = storeResource.get();
    assertNotNull(all);
    assertTrue(all.size() >= 3, "import.sql provides initial stores");
  }

  @Test
  public void testGetSingle_found() {
    Store s = storeResource.getSingle(1L);
    assertNotNull(s);
    assertEquals("TONSTAD", s.name);
  }

  @Test
  public void testCreateAndDelete_flow() {
    Store s = new Store("happyCreate");
    Response resp = storeResource.create(s);
    assertEquals(201, resp.getStatus());
    Store created = (Store) resp.getEntity();
    assertNotNull(created.id);

    // cleanup
    Response del = storeResource.delete(created.id);
    assertEquals(204, del.getStatus());
  }

  @Test
  public void testUpdate_success() {
    Store s = new Store("forUpdateTest");
    var r = storeResource.create(s);
    Store created = (Store) r.getEntity();

    Store updated = new Store();
    updated.name = "updatedName";
    updated.quantityProductsInStock = 77;

    Store res = storeResource.update(created.id, updated);
    assertEquals("updatedName", res.name);
    assertEquals(77, res.quantityProductsInStock);

    storeResource.delete(created.id);
  }
}
