package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceTargetedIT {

  @Inject
  StoreResource resource;

  @Test
  public void create_withIdSet_badRequest() {
    Store s = new Store("test-create-id");
    s.id = 9999L; // invalid for create

    WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class, () -> resource.create(s));
    Response r = ex.getResponse();
    Assertions.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
  }

  @Test
  public void full_lifecycle_create_get_delete_getNotFound() {
    Store s = new Store("tl-store-1");
    Response created = resource.create(s);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), created.getStatus());
    Store returned = (Store) created.getEntity();
    Assertions.assertNotNull(returned.id);

    // getSingle should return the entity
    Store fetched = resource.getSingle(returned.id);
    Assertions.assertEquals("tl-store-1", fetched.name);

    // delete
    Response del = resource.delete(returned.id);
    Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), del.getStatus());

    // subsequent get should throw NOT_FOUND
    WebApplicationException notFound = Assertions.assertThrows(WebApplicationException.class, () -> resource.getSingle(returned.id));
    Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), notFound.getResponse().getStatus());
  }

  @Test
  public void update_and_patch_error_paths_and_partial() {
    // create baseline
    Store base = new Store("up-store-1");
    Response created = resource.create(base);
    Store saved = (Store) created.getEntity();

    // update with null body -> bad request
    WebApplicationException ex1 = Assertions.assertThrows(WebApplicationException.class, () -> resource.update(saved.id, null));
    Assertions.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex1.getResponse().getStatus());

    // update not found
    Store u = new Store("does-not-matter");
    u.name = "x";
    WebApplicationException ex2 = Assertions.assertThrows(WebApplicationException.class, () -> resource.update(999999L, u));
    Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex2.getResponse().getStatus());

    // patch with null body -> bad request
    WebApplicationException ex3 = Assertions.assertThrows(WebApplicationException.class, () -> resource.patch(saved.id, null));
    Assertions.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex3.getResponse().getStatus());

    // patch partial update (name changed, quantity stays)
    Store patch = new Store();
    patch.name = "patched-name";
    patch.quantityProductsInStock = 0; // should be treated as not-provided

    Store afterPatch = resource.patch(saved.id, patch);
    Assertions.assertEquals("patched-name", afterPatch.name);

    // patch quantity update
    Store patchQty = new Store();
    patchQty.quantityProductsInStock = 123;
    Store afterQty = resource.patch(saved.id, patchQty);
    Assertions.assertEquals(123, afterQty.quantityProductsInStock);
  }
}
