package com.fulfilment.application.monolith.fulfilment.domain;

public class FulfilmentAssignment {

    public String storeId;
    public String productId;
    public String warehouseBusinessUnitCode;

    public FulfilmentAssignment(String storeId,
                                String productId,
                                String warehouseBusinessUnitCode) {
        this.storeId = storeId;
        this.productId = productId;
        this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    }
}
