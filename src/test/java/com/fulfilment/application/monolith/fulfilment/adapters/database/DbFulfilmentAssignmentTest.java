package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentAssignment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DbFulfilmentAssignmentTest {

    @Test
    public void testConstructor() {
        DbFulfilmentAssignment db = new DbFulfilmentAssignment("S1", "P1", "W1");
        assertEquals("S1", db.storeId);
        assertEquals("P1", db.productId);
        assertEquals("W1", db.warehouseBusinessUnitCode);
    }

    @Test
    public void testDefaultConstructor() {
        DbFulfilmentAssignment db = new DbFulfilmentAssignment();
        assertNull(db.storeId);
        assertNull(db.productId);
        assertNull(db.warehouseBusinessUnitCode);
    }

    @Test
    public void testToFulfilmentAssignment() {
        DbFulfilmentAssignment db = new DbFulfilmentAssignment("S2", "P2", "W2");
        FulfilmentAssignment assignment = db.toFulfilmentAssignment();
        
        assertNotNull(assignment);
        assertEquals("S2", assignment.storeId);
        assertEquals("P2", assignment.productId);
        assertEquals("W2", assignment.warehouseBusinessUnitCode);
    }

    @Test
    public void testFromFulfilmentAssignment() {
        FulfilmentAssignment assignment = new FulfilmentAssignment("S3", "P3", "W3");
        DbFulfilmentAssignment db = DbFulfilmentAssignment.fromFulfilmentAssignment(assignment);
        
        assertNotNull(db);
        assertEquals("S3", db.storeId);
        assertEquals("P3", db.productId);
        assertEquals("W3", db.warehouseBusinessUnitCode);
    }

    @Test
    public void testRoundTrip() {
        FulfilmentAssignment original = new FulfilmentAssignment("S4", "P4", "W4");
        DbFulfilmentAssignment db = DbFulfilmentAssignment.fromFulfilmentAssignment(original);
        FulfilmentAssignment converted = db.toFulfilmentAssignment();
        
        assertEquals(original.storeId, converted.storeId);
        assertEquals(original.productId, converted.productId);
        assertEquals(original.warehouseBusinessUnitCode, converted.warehouseBusinessUnitCode);
    }
}
