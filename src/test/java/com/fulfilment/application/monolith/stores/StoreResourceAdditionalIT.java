package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreResourceAdditionalIT {

  @Inject
  StoreResource resource;

  @Test
  @Transactional
  public void delete_happyPath_and_notFound() {
    Store s = new Store("to-delete");
    s.persist();

    Response r = resource.delete(s.id);
    assertEquals(Response.Status.NO_CONTENT.getStatusCode(), r.getStatus());

    // deleting again should throw 404
    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.delete(s.id));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  public void update_withNullName_throwsBadRequest() {
    Store updated = new Store();
    updated.name = null;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.update(1L, updated));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  public void patch_withNullBody_throwsBadRequest() {
    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.patch(1L, null));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  public void create_withId_throws422() {
    Store s = new Store();
    s.id = 999L;
    s.name = "BAD";
    s.quantityProductsInStock = 1;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.create(s));

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  public void patch_respects_quantity_zero_and_nonzero_branches() {
    // create a store with quantity 0
    Store toCreate = new Store();
    toCreate.name = "TEST_STORE";
    toCreate.quantityProductsInStock = 0;

    Response r = resource.create(toCreate);
    // created entity should be returned
    Store created = (Store) r.getEntity();
    assertNotNull(created);
    Long id = created.id;

    // patch: because original quantity == 0, patch should NOT change quantity
    Store patchReq = new Store();
    patchReq.name = "NEW_NAME";
    patchReq.quantityProductsInStock = 5;

    Store patched = resource.patch(id, patchReq);
    assertEquals("NEW_NAME", patched.name);
    // quantity should remain 0 because original was 0
    assertEquals(0, patched.quantityProductsInStock);

    // update the entity so quantity becomes non-zero
    Store upd = new Store();
    upd.name = "NEW_NAME";
    upd.quantityProductsInStock = 3;
    Store afterUpdate = resource.update(id, upd);
    assertEquals(3, afterUpdate.quantityProductsInStock);

    // now patch should update quantity because existing quantity != 0
    Store patchReq2 = new Store();
    patchReq2.name = "NEW_NAME2";
    patchReq2.quantityProductsInStock = 7;

    Store patched2 = resource.patch(id, patchReq2);
    assertEquals("NEW_NAME2", patched2.name);
    assertEquals(7, patched2.quantityProductsInStock);
  }
}
