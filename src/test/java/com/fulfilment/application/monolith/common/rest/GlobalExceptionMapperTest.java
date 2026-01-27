package com.fulfilment.application.monolith.common.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

public class GlobalExceptionMapperTest {

  private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

  @Test
  public void mapsValidationExceptionTo400() {
    Response response = mapper.toResponse(new ValidationException("bad"));
    assertEquals(400, response.getStatus());
    assertEquals("bad", response.getEntity());
  }

  @Test
  public void mapsNotFoundExceptionTo404() {
    Response response = mapper.toResponse(new NotFoundException("missing"));
    assertEquals(404, response.getStatus());
    assertEquals("missing", response.getEntity());
  }

  @Test
  public void mapsConflictExceptionTo409() {
    Response response = mapper.toResponse(new ConflictException("conflict"));
    assertEquals(409, response.getStatus());
    assertEquals("conflict", response.getEntity());
  }

  @Test
  public void mapsGenericExceptionTo500() {
    Response response = mapper.toResponse(new RuntimeException("boom"));
    assertEquals(500, response.getStatus());
    assertEquals(AppConstants.ERR_INTERNAL_SERVER, response.getEntity());
  }
}
