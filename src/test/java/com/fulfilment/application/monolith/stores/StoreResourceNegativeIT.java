package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreResourceNegativeIT {

  @Inject
  StoreResource resource;

  @Test
  public void getSingle_notFound_throws404() {
    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> {
      resource.getSingle(99999L);
    });
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  public void update_missingName_throws422() {
    // create a store to have an id
    Store s = new Store();
    s.name = "TEMP";
    s.quantityProductsInStock = 1;
    resource.create(s);

    // attempt update with missing name
    Store bad = new Store();
    bad.name = null;
    bad.quantityProductsInStock = 2;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> {
      resource.update(s.id, bad);
    });

    assertEquals(422, ex.getResponse().getStatus());
  }

  @Test
  public void delete_notFound_throws404() {
    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> {
      resource.delete(999999L);
    });
    assertEquals(404, ex.getResponse().getStatus());
  }
}
