package com.fulfilment.application.monolith.stores;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreResourceMoreCoverageIT {

  @Inject
  StoreResource storeResource;

  @Test
  public void testPatch_quantityZero_doesNotChangeQuantity() {
    Store s = new Store("patchZero");

    // quantity defaults to 0
    var resp = storeResource.create(s);
    Store created = (Store) resp.getEntity();

    Store updated = new Store();
    updated.name = "patched";
    updated.quantityProductsInStock = 99;

    Store res = storeResource.patch(created.id, updated);
  assertEquals("patched", res.name);
  // since original quantity was 0, patch should NOT change it
  assertEquals(0, res.quantityProductsInStock);

    storeResource.delete(created.id);
  }

  @Test
  public void testPatch_quantityNonZero_updatesQuantity() {
    Store s = new Store("patchNonZero");
    s.quantityProductsInStock = 10;

    var resp = storeResource.create(s);
    Store created = (Store) resp.getEntity();

    Store updated = new Store();
    updated.name = "patched2";
    updated.quantityProductsInStock = 20;

    Store res = storeResource.patch(created.id, updated);
  assertEquals("patched2", res.name);
  assertEquals(20, res.quantityProductsInStock);

    storeResource.delete(created.id);
  }

  @Test
  public void testUpdate_notFound_throws404() {
    Store updated = new Store();
    updated.name = "nope";
    assertThrows(WebApplicationException.class, () -> storeResource.update(9_999_999L, updated));
  }

  @Test
  public void testDelete_notFound_throws404() {
    assertThrows(WebApplicationException.class, () -> storeResource.delete(9_999_998L));
  }

  @Test
  public void testCreate_withId_throws422() {
    Store s = new Store("bad");
    s.id = 123L;
    assertThrows(WebApplicationException.class, () -> storeResource.create(s));
  }

  @Test
  public void testGetSingle_notFound_throws404() {
    assertThrows(WebApplicationException.class, () -> storeResource.getSingle(9_999_997L));
  }

  @Test
  public void testUpdate_missingName_throws422() {
    Store updated = new Store();
    updated.name = null;
    assertThrows(WebApplicationException.class, () -> storeResource.update(1L, updated));
  }
}
