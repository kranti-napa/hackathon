package com.fulfilment.application.monolith.fulfilment.adapters.rest;

import com.fulfilment.application.monolith.fulfilment.domain.usecases
        .AssignWarehouseToStoreProductUseCase;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Path("/fulfilment")
@Consumes("application/json")
public class FulfilmentResource {

    private final AssignWarehouseToStoreProductUseCase useCase;

    public FulfilmentResource(
            AssignWarehouseToStoreProductUseCase useCase) {
        this.useCase = useCase;
    }

    @POST
    public Response assign(FulfilmentRequest request) {

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
