package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentAssignment;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class FulfilmentAssignmentRepositoryTest {

    @Inject
    FulfilmentAssignmentRepository repository;

    @BeforeEach
    @Transactional
    public void cleanup() {
        repository.deleteAll();
    }

    @Test
    @Transactional
    public void testCreateAndGetAll() {
        FulfilmentAssignment assignment = new FulfilmentAssignment("S1", "P1", "W1");
        repository.create(assignment);

        List<FulfilmentAssignment> all = repository.getAll();
        assertEquals(1, all.size());
        assertEquals("S1", all.get(0).storeId);
        assertEquals("P1", all.get(0).productId);
        assertEquals("W1", all.get(0).warehouseBusinessUnitCode);
    }

    @Test
    @Transactional
    public void testGetAllReturnsEmptyListWhenNoAssignments() {
        List<FulfilmentAssignment> all = repository.getAll();
        assertNotNull(all);
        assertEquals(0, all.size());
    }

    @Test
    @Transactional
    public void testCreateMultipleAssignments() {
        repository.create(new FulfilmentAssignment("S1", "P1", "W1"));
        repository.create(new FulfilmentAssignment("S1", "P2", "W2"));
        repository.create(new FulfilmentAssignment("S2", "P1", "W1"));

        List<FulfilmentAssignment> all = repository.getAll();
        assertEquals(3, all.size());
    }

    @Test
    @Transactional
    public void testCreateNullAssignment() {
        // Should not throw, just log warning
        repository.create(null);
        
        List<FulfilmentAssignment> all = repository.getAll();
        assertEquals(0, all.size());
    }
}
