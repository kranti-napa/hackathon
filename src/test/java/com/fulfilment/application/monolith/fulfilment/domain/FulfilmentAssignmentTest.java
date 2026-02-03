package com.fulfilment.application.monolith.fulfilment.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FulfilmentAssignmentTest {

    @Test
    public void testConstructor() {
        FulfilmentAssignment assignment = new FulfilmentAssignment("S1", "P1", "W1");
        
        assertEquals("S1", assignment.storeId);
        assertEquals("P1", assignment.productId);
        assertEquals("W1", assignment.warehouseBusinessUnitCode);
    }

    @Test
    public void testWithNullValues() {
        FulfilmentAssignment assignment = new FulfilmentAssignment(null, null, null);
        
        assertNull(assignment.storeId);
        assertNull(assignment.productId);
        assertNull(assignment.warehouseBusinessUnitCode);
    }

    @Test
    public void testWithEmptyStrings() {
        FulfilmentAssignment assignment = new FulfilmentAssignment("", "", "");
        
        assertEquals("", assignment.storeId);
        assertEquals("", assignment.productId);
        assertEquals("", assignment.warehouseBusinessUnitCode);
    }

    @Test
    public void testFieldModification() {
        FulfilmentAssignment assignment = new FulfilmentAssignment("S1", "P1", "W1");
        
        assignment.storeId = "S2";
        assignment.productId = "P2";
        assignment.warehouseBusinessUnitCode = "W2";
        
        assertEquals("S2", assignment.storeId);
        assertEquals("P2", assignment.productId);
        assertEquals("W2", assignment.warehouseBusinessUnitCode);
    }

    @Test
    public void testLongStringValues() {
        String longString = "A".repeat(100);
        FulfilmentAssignment assignment = new FulfilmentAssignment(longString, longString, longString);
        
        assertEquals(longString, assignment.storeId);
        assertEquals(longString, assignment.productId);
        assertEquals(longString, assignment.warehouseBusinessUnitCode);
    }
}
