package com.fulfilment.application.monolith.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProductErrorMapperTest {

  @Test
  public void testErrorMapperHandlesWebAppAndGeneric() throws Exception {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    ObjectMapper om = new ObjectMapper();
    java.lang.reflect.Field f = ProductResource.ErrorMapper.class.getDeclaredField("objectMapper");
    f.setAccessible(true);
    f.set(mapper, om);

    WebApplicationException webEx = new WebApplicationException("not found", 404);
    Response r = mapper.toResponse(webEx);
    Assertions.assertEquals(404, r.getStatus());

    Object entity = r.getEntity();
    ObjectNode node = null;
    if (entity instanceof ObjectNode) {
      node = (ObjectNode) entity;
    } else if (entity instanceof byte[]) {
      node = (ObjectNode) om.readTree((byte[]) entity);
    } else {
      node = (ObjectNode) om.readTree(entity.toString());
    }

    Assertions.assertTrue(node.has("code"));

    RuntimeException re = new RuntimeException("boom");
    Response r2 = mapper.toResponse(re);
    Assertions.assertEquals(500, r2.getStatus());
  }
}
