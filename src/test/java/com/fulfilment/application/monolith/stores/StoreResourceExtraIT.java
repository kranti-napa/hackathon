package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
public class StoreResourceExtraIT {

  @Inject
  StoreResource resource;

  @Test
  public void get_list_returns_sorted_list_and_is_not_null() {
    // create two stores to ensure listAll has something to return
    Store a = new Store("aaa-store");
    Store b = new Store("zzz-store");

    Response r1 = resource.create(a);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), r1.getStatus());
    Response r2 = resource.create(b);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), r2.getStatus());

    List<Store> list = resource.get();
    Assertions.assertNotNull(list);
    // at least the two we created should be present
    Assertions.assertTrue(list.stream().anyMatch(s -> "aaa-store".equals(s.name)));
    Assertions.assertTrue(list.stream().anyMatch(s -> "zzz-store".equals(s.name)));
  }

  @Test
  public void create_null_throwsNullPointer() {
    Assertions.assertThrows(NullPointerException.class, () -> resource.create(null));
  }

  @Test
  public void update_success_applies_changes() {
    Store base = new Store("original-name");
    base.quantityProductsInStock = 5;
    Response created = resource.create(base);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), created.getStatus());
    Store saved = (Store) created.getEntity();

    Store updated = new Store();
    updated.name = "updated-name";
    updated.quantityProductsInStock = 42;

    Store after = resource.update(saved.id, updated);
    Assertions.assertEquals("updated-name", after.name);
    Assertions.assertEquals(42, after.quantityProductsInStock);
  }

  @Test
  public void delete_notFound_throwsNotFound() {
    WebApplicationException ex = Assertions.assertThrows(WebApplicationException.class, () -> resource.delete(999999L));
    Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
  }
}
