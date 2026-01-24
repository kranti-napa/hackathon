package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceIntegrationTest {

  @Inject StoreResource storeResource;

  @Test
  public void delete_nonExistent_throwsNotFound() {
    // pick a high id that won't exist in the in-memory DB
    long id = 9_999_999L;
    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> storeResource.delete(id));
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
  }

  @Test
  public void patch_nullBody_throwsBadRequest() {
    // patch should validate the body before touching the DB
    long anyId = 1L;
    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> storeResource.patch(anyId, null));
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
  }
}
