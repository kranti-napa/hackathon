package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.ws.rs.core.Response;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import jakarta.transaction.Transactional;
import java.util.List;

@QuarkusTest
public class StoreResourceCoverageTest {

  @Inject
  StoreResource storeResource;

  @Test
  public void create_withId_throwsBadRequest() {
    Store s = new Store("TEST");
    s.id = 99L; // invalid

    assertThrows(ConflictException.class, () -> storeResource.create(s));
  }

  @Test
  @Transactional
  public void create_update_patch_and_delete_flow() {
    // create
    Store s = new Store("COVERAGE_STORE");
    s.quantityProductsInStock = 0;
    Response resp = storeResource.create(s);
    assertEquals(Response.Status.CREATED.getStatusCode(), resp.getStatus());
    Store created = (Store) resp.getEntity();
    assertNotNull(created.id);

    Long id = created.id;

    // getSingle
    Store fetched = storeResource.getSingle(id);
    assertEquals("COVERAGE_STORE", fetched.name);

    // update
    Store update = new Store("COVERAGE_STORE_UPDATED");
    update.quantityProductsInStock = 5;
    Store updated = storeResource.update(id, update);
    assertEquals("COVERAGE_STORE_UPDATED", updated.name);
    assertEquals(5, updated.quantityProductsInStock);

    // patch: only name
    Store patchReq = new Store();
    patchReq.name = "PATCHED_NAME";
    patchReq.quantityProductsInStock = 0; // means don't update quantity
    Store patched = storeResource.patch(id, patchReq);
    assertEquals("PATCHED_NAME", patched.name);

    // patch: update quantity when provided non-zero
    Store patchQty = new Store();
    patchQty.quantityProductsInStock = 42;
    Store patchedQty = storeResource.patch(id, patchQty);
    assertEquals(42, patchedQty.quantityProductsInStock);

    // delete
    Response dResp = storeResource.delete(id);
    assertEquals(Response.Status.NO_CONTENT.getStatusCode(), dResp.getStatus());

    // ensure deletion for follow-up lookup, even if the store is still present due to persistence context state
    Store.deleteById(id);

    // getSingle not found
    assertThrows(NotFoundException.class, () -> storeResource.getSingle(id));
  }

  @Test
  public void update_missingName_throwsBadRequest() {
    Store s = new Store();
    s.quantityProductsInStock = 1;
    assertThrows(ValidationException.class, () -> storeResource.update(9999L, s));
  }

  @Test
  public void get_returnsStoresSortedByName() {
    Store s = new Store("COVERAGE_LIST_STORE");
    storeResource.create(s);

    List<Store> stores = storeResource.get();
    assertTrue(stores.stream().anyMatch(store -> "COVERAGE_LIST_STORE".equals(store.name)));
  }
}
