package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class ErrorMapperTest {

    @Test
    void shouldMapWebApplicationException() {
        StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
        mapper.objectMapper = new ObjectMapper();

        WebApplicationException ex =
            new WebApplicationException(
                Response.status(404).entity("Not found").build()
            );

        Response response = mapper.toResponse(ex);

        assertEquals(404, response.getStatus());
        String body = response.getEntity().toString();

        assertTrue(body.contains("exceptionType"));
        assertTrue(body.contains("code"));
    }

    @Test
    void shouldMapGenericExceptionTo500() {
        StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
        mapper.objectMapper = new ObjectMapper();

        Exception ex = new RuntimeException("Boom");

        Response response = mapper.toResponse(ex);

        assertEquals(500, response.getStatus());
        String body = response.getEntity().toString();

        assertTrue(body.contains("Boom"));
    }
}

