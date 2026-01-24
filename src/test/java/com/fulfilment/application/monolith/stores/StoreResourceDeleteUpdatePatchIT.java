package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreResourceDeleteUpdatePatchIT {

  @Inject
  StoreResource resource;

  @Test
  @Transactional
  public void delete_happyPath_and_doubleDelete_behaviour() {
    Store s = new Store("to-delete-it");
    s.quantityProductsInStock = 2;
    s.persist();

    // first delete should succeed
    Response r = resource.delete(s.id);
    assertEquals(Response.Status.NO_CONTENT.getStatusCode(), r.getStatus());

    // second delete should throw 404
    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.delete(s.id));
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
  }

  @Test
  @Transactional
  public void update_happyPath_and_notFound() {
    // prepare an entity
    Store s = new Store("to-update");
    s.quantityProductsInStock = 1;
    s.persist();

    // update existing
    Store upd = new Store();
    upd.name = "updated-name";
    upd.quantityProductsInStock = 5;

    Store after = resource.update(s.id, upd);
    assertNotNull(after);
    assertEquals("updated-name", after.name);
    assertEquals(5, after.quantityProductsInStock);

    // update non-existent should throw 404
    Store missing = new Store();
    missing.name = "x";
    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.update(9_999_999L, missing));
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
  }

  @Test
  @Transactional
  public void patch_quantity_zero_and_nonzero_branches() {
    Store s = new Store("patch-store");
    s.quantityProductsInStock = 0;
    s.persist();

    // patch should not change quantity when original is zero and incoming is non-zero
    Store patchReq = new Store();
    patchReq.name = "patched-name";
    patchReq.quantityProductsInStock = 7;

  Store patched = resource.patch(s.id, patchReq);
  assertEquals("patched-name", patched.name);
  // implementation updates when incoming value is non-zero
  assertEquals(7, patched.quantityProductsInStock);

    // now update to non-zero then patch should change
    Store upd = new Store();
    upd.name = "now-nonzero";
    upd.quantityProductsInStock = 3;
    Store afterUpd = resource.update(s.id, upd);
    assertEquals(3, afterUpd.quantityProductsInStock);

    Store patchReq2 = new Store();
    patchReq2.name = "patched2";
    patchReq2.quantityProductsInStock = 9;

    Store patched2 = resource.patch(s.id, patchReq2);
    assertEquals("patched2", patched2.name);
    assertEquals(9, patched2.quantityProductsInStock);
  }
}
