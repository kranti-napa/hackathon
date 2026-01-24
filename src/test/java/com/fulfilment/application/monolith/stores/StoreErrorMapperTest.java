package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StoreErrorMapperTest {

  @Test
  public void testStoreErrorMapper() throws Exception {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    ObjectMapper om = new ObjectMapper();
    java.lang.reflect.Field f = StoreResource.ErrorMapper.class.getDeclaredField("objectMapper");
    f.setAccessible(true);
    f.set(mapper, om);

    WebApplicationException webEx = new WebApplicationException("not found", 404);
    Response r = mapper.toResponse(webEx);
    Assertions.assertEquals(404, r.getStatus());

    RuntimeException re = new RuntimeException("oops");
    Response r2 = mapper.toResponse(re);
    Assertions.assertEquals(500, r2.getStatus());
  }
}
