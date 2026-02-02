package com.fulfilment.application.monolith.fulfilment.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.fulfilment.adapters.inmemory.InMemoryFulfilmentAssignmentStore;
import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AssignWarehouseToStoreProductUseCaseTest {

  InMemoryFulfilmentAssignmentStore store;
  AssignWarehouseToStoreProductUseCase useCase;

  @BeforeEach
  public void setup() {
    store = new InMemoryFulfilmentAssignmentStore();
    useCase = new AssignWarehouseToStoreProductUseCase(store);
  }

  @Test
  public void testHappyPathAssign() {
    useCase.assign("S1","P1","W1");
    assertEquals(1, store.getAll().size());
  }

  @Test
  public void testProductMaxTwoWarehouses() {
    store.create(new FulfilmentAssignment("S1","P1","W1"));
    store.create(new FulfilmentAssignment("S1","P1","W2"));

    assertThrows(ConflictException.class, () -> useCase.assign("S1","P1","W3"));
  }

  @Test
  public void testStoreMaxThreeWarehouses() {
    store.create(new FulfilmentAssignment("S2","P1","W1"));
    store.create(new FulfilmentAssignment("S2","P2","W2"));
    store.create(new FulfilmentAssignment("S2","P3","W3"));

    assertThrows(ConflictException.class, () -> useCase.assign("S2","P4","W4"));
  }

  @Test
  public void testWarehouseMaxFiveProducts() {
    store.create(new FulfilmentAssignment("S3","P1","WX"));
    store.create(new FulfilmentAssignment("S3","P2","WX"));
    store.create(new FulfilmentAssignment("S3","P3","WX"));
    store.create(new FulfilmentAssignment("S3","P4","WX"));
    store.create(new FulfilmentAssignment("S3","P5","WX"));

    assertThrows(ConflictException.class, () -> useCase.assign("S4","P6","WX"));
  }

  @Test
  public void testStoreMaxThreeWarehouses_allowsExistingWarehouse() {
    store.create(new FulfilmentAssignment("S2","P1","W1"));
    store.create(new FulfilmentAssignment("S2","P2","W2"));
    store.create(new FulfilmentAssignment("S2","P3","W3"));

    useCase.assign("S2", "P4", "W1");

    assertEquals(4, store.getAll().size());
  }

  @Test
  public void testProductMaxTwoWarehouses_allowsExistingWarehouse() {
    store.create(new FulfilmentAssignment("S1","P1","W1"));
    store.create(new FulfilmentAssignment("S1","P1","W2"));

    useCase.assign("S1", "P1", "W2");

    assertEquals(2, store.getAll().size());
  }

  @Test
  public void testSinglePassOptimization_largeDataset() {
    // This test verifies that the single-pass algorithm works correctly with large datasets
    // Previously, this would have done multiple full scans (O(n*k) complexity)
    // Now it does a single scan (O(n) complexity)
    
    // Create a large number of assignments
    for (int s = 1; s <= 10; s++) {
      for (int p = 1; p <= 20; p++) {
        for (int w = 1; w <= 3; w++) {
          if (s * p * w <= 100) { // Create ~100 assignments
            store.create(new FulfilmentAssignment(
                "S" + s,
                "P" + p,
                "W" + w
            ));
          }
        }
      }
    }

    // Verify constraints still work correctly with optimized single-pass
    assertThrows(ConflictException.class, () -> useCase.assign("S1", "P1", "W4"));
    assertThrows(ConflictException.class, () -> useCase.assign("S1", "P21", "W1"));
    assertThrows(ConflictException.class, () -> useCase.assign("S11", "P1", "W1"));
  }
}
