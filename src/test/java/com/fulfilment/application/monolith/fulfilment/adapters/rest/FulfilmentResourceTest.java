package com.fulfilment.application.monolith.fulfilment.adapters.rest;

import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import com.fulfilment.application.monolith.fulfilment.domain.usecases.AssignWarehouseToStoreProductUseCase;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FulfilmentResourceTest {

  static class StubUseCase extends AssignWarehouseToStoreProductUseCase {
    public String s, p, b;

    public StubUseCase() {
      super(null);
    }

    @Override
    public void assign(String storeId, String productId, String warehouseBusinessUnitCode) {
      this.s = storeId;
      this.p = productId;
      this.b = warehouseBusinessUnitCode;
    }
  }

  @Test
  public void testAssignReturnsCreatedAndCallsUseCase() {
    StubUseCase stub = new StubUseCase();
    FulfilmentResource r = new FulfilmentResource(stub);

    FulfilmentResource.FulfilmentRequest req = new FulfilmentResource.FulfilmentRequest();
    req.storeId = "S1";
    req.productId = "P1";
    req.warehouseBusinessUnitCode = "BU1";

    Response resp = r.assign(req);
    Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), resp.getStatus());
    Assertions.assertEquals("S1", stub.s);
    Assertions.assertEquals("P1", stub.p);
    Assertions.assertEquals("BU1", stub.b);
  }

  @Test
  public void testAssignWithNullRequestThrows() {
    StubUseCase stub = new StubUseCase();
    FulfilmentResource r = new FulfilmentResource(stub);

    assertThrows(ValidationException.class, () -> r.assign(null));
  }

  @Test
  public void testFulfilmentRequestFields() {
    FulfilmentResource.FulfilmentRequest req = new FulfilmentResource.FulfilmentRequest();
    req.storeId = "S100";
    req.productId = "P200";
    req.warehouseBusinessUnitCode = "W300";

    assertEquals("S100", req.storeId);
    assertEquals("P200", req.productId);
    assertEquals("W300", req.warehouseBusinessUnitCode);
  }
}
