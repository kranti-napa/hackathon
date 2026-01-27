package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceIntegrationTest {

  @Inject StoreResource storeResource;

  @Test
  public void delete_nonExistent_throwsNotFound() {
    // pick a high id that won't exist in the in-memory DB
    long id = 9_999_999L;
    assertThrows(NotFoundException.class, () -> storeResource.delete(id));
  }

  @Test
  public void patch_nullBody_throwsBadRequest() {
    // patch should validate the body before touching the DB
    long anyId = 1L;
    assertThrows(ValidationException.class, () -> storeResource.patch(anyId, null));
  }
}
