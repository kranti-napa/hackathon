package com.fulfilment.application.monolith.common.rest;

import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable ex) {

        if (ex instanceof ValidationException) {
            return Response.status(400).entity(ex.getMessage()).build();
        }

        if (ex instanceof NotFoundException) {
            return Response.status(404).entity(ex.getMessage()).build();
        }

        if (ex instanceof ConflictException) {
            return Response.status(409).entity(ex.getMessage()).build();
        }

        return Response
                .status(500)
                .entity(AppConstants.ERR_INTERNAL_SERVER)
                .build();
    }
}
