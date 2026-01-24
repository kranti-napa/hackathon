package com.fulfilment.application.monolith.stores;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreResourceIT {

  @Inject
  StoreResource storeResource;

  @Test
  public void createGetDeleteFlow() {
    Store store = new Store("TestStoreIT");

    Response resp = storeResource.create(store);
    assertEquals(201, resp.getStatus());

    Object entity = resp.getEntity();
    assertNotNull(entity);
    assertTrue(entity instanceof Store);
    Store created = (Store) entity;
    assertNotNull(created.id);

    List<Store> all = storeResource.get();
    assertTrue(all.stream().anyMatch(s -> s.id.equals(created.id)));

    Response del = storeResource.delete(created.id);
    assertEquals(204, del.getStatus());

    // getSingle should now throw 404
    assertThrows(WebApplicationException.class, () -> storeResource.getSingle(created.id));
  }

  @Test
  public void createWithIdFails() {
    Store s = new Store("Bad");
    s.id = 999L;
    assertThrows(WebApplicationException.class, () -> storeResource.create(s));
  }

  @Test
  public void updateFlow() {
    Store s = new Store("UpStoreIT");
    Response resp = storeResource.create(s);
    Store created = (Store) resp.getEntity();

    Store updated = new Store();
    updated.name = "UpStoreIT-New";
    updated.quantityProductsInStock = 7;

    Store res = storeResource.update(created.id, updated);
    assertEquals("UpStoreIT-New", res.name);

    // cleanup
    storeResource.delete(created.id);
  }
}
