package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class StoreResourceErrorMapperTest {

  @Test
  public void mapsWebApplicationException_withStatusFromException() throws Exception {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();

    // inject ObjectMapper
    Field omField = StoreResource.ErrorMapper.class.getDeclaredField("objectMapper");
    omField.setAccessible(true);
    omField.set(mapper, new ObjectMapper());

    WebApplicationException wae = new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("nope").build());

    Response resp = mapper.toResponse(wae);
    Assertions.assertEquals(404, resp.getStatus());
    ObjectNode node = (ObjectNode) resp.getEntity();
    Assertions.assertTrue(node.has("exceptionType"));
    Assertions.assertEquals(404, node.get("code").asInt());
    Assertions.assertTrue(node.has("error"));
  }

  @Test
  public void mapsGenericException_to500_and_omitsNullMessage() throws Exception {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    Field omField = StoreResource.ErrorMapper.class.getDeclaredField("objectMapper");
    omField.setAccessible(true);
    omField.set(mapper, new ObjectMapper());

    Exception eWithMsg = new Exception("boom");
    Response r = mapper.toResponse(eWithMsg);
    Assertions.assertEquals(500, r.getStatus());
    ObjectNode n = (ObjectNode) r.getEntity();
    Assertions.assertEquals("boom", n.get("error").asText());

    Exception eNoMsg = new Exception();
    Response r2 = mapper.toResponse(eNoMsg);
    Assertions.assertEquals(500, r2.getStatus());
    ObjectNode n2 = (ObjectNode) r2.getEntity();
    // message is null so no 'error' field present
    Assertions.assertFalse(n2.has("error"));
  }
}
