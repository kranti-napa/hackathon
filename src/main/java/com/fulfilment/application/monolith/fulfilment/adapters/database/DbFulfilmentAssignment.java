package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentAssignment;
import jakarta.persistence.*;

@Entity
@Table(name = "fulfilment_assignment")
public class DbFulfilmentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fulfilment_assignment_seq")
    @SequenceGenerator(name = "fulfilment_assignment_seq", sequenceName = "fulfilment_assignment_seq", allocationSize = 1)
    public Long id;

    @Column(name = "store_id", nullable = false)
    public String storeId;

    @Column(name = "product_id", nullable = false)
    public String productId;

    @Column(name = "warehouse_business_unit_code", nullable = false)
    public String warehouseBusinessUnitCode;

    public DbFulfilmentAssignment() {
    }

    public DbFulfilmentAssignment(String storeId, String productId, String warehouseBusinessUnitCode) {
        this.storeId = storeId;
        this.productId = productId;
        this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    }

    public FulfilmentAssignment toFulfilmentAssignment() {
        return new FulfilmentAssignment(
            this.storeId,
            this.productId,
            this.warehouseBusinessUnitCode
        );
    }

    public static DbFulfilmentAssignment fromFulfilmentAssignment(FulfilmentAssignment assignment) {
        return new DbFulfilmentAssignment(
            assignment.storeId,
            assignment.productId,
            assignment.warehouseBusinessUnitCode
        );
    }
}
