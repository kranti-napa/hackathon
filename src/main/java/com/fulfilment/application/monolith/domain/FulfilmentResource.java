package com.fulfilment.application.monolith.fulfilment.adapters.rest;

import com.fulfilment.application.monolith.fulfilment.domain.usecases
        .AssignWarehouseToStoreProductUseCase;
import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(FulfilmentResource.PATH_FULFILMENT)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfilmentResource {

    static final String PATH_FULFILMENT = "/fulfilment";

    private final AssignWarehouseToStoreProductUseCase useCase;

    public FulfilmentResource(
            AssignWarehouseToStoreProductUseCase useCase) {
        this.useCase = useCase;
    }

    @POST
    public Response assign(FulfilmentRequest request) {

        if (request == null) {
            throw new ValidationException(AppConstants.ERR_FULFILMENT_REQUEST_NULL);
        }

        useCase.assign(
            request.storeId,
            request.productId,
            request.warehouseBusinessUnitCode
        );

        return Response.status(Response.Status.CREATED).build();
    }

    public static class FulfilmentRequest {
        public String storeId;
        public String productId;
        public String warehouseBusinessUnitCode;
    }
}
