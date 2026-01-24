package com.fulfilment.application.monolith.stores;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreResourceMoreIT {

  @Inject
  StoreResource storeResource;

  @Test
  public void getSingleNotFound() {
    assertThrows(WebApplicationException.class, () -> storeResource.getSingle(99999L));
  }

  @Test
  public void updateNotFound() {
    Store s = new Store();
    s.name = "ShouldFail";
    assertThrows(WebApplicationException.class, () -> storeResource.update(99999L, s));
  }

  @Test
  public void updateNullNameThrows422() {
    Store s = new Store("NameForUpdate");
    var resp = storeResource.create(s);
    Store created = (Store) resp.getEntity();

    Store upd = new Store();
    upd.name = null; // should trigger 422
    assertThrows(WebApplicationException.class, () -> storeResource.update(created.id, upd));

    // cleanup
    storeResource.delete(created.id);
  }

  @Test
  public void patchNullNameThrows422() {
    Store s = new Store("NameForPatch");
    var resp = storeResource.create(s);
    Store created = (Store) resp.getEntity();

    Store upd = new Store();
    upd.name = null;
    assertThrows(WebApplicationException.class, () -> storeResource.patch(created.id, upd));

    storeResource.delete(created.id);
  }

  @Test
  public void patchUpdatesQuantityWhenEntityHasNonZeroQuantity() {
    Store s = new Store("NameQty");
    s.quantityProductsInStock = 2; // non-zero so patch branch will update quantity
    var resp = storeResource.create(s);
    Store created = (Store) resp.getEntity();

    Store upd = new Store();
    upd.name = "NameQtyNew";
    upd.quantityProductsInStock = 9;

    Store patched = storeResource.patch(created.id, upd);
    assertEquals("NameQtyNew", patched.name);
    assertEquals(9, patched.quantityProductsInStock);

    storeResource.delete(created.id);
  }

  @Test
  public void deleteNotFoundThrows404() {
    assertThrows(WebApplicationException.class, () -> storeResource.delete(999999L));
  }
}
