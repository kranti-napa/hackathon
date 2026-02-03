package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.time.LocalDateTime;

@QuarkusTest
class WarehouseRepositoryTest {

  @Inject
  WarehouseRepository repository;

  @Test
  @Transactional
  void update_nonExistent_throwsAnd_update_success() {
    Warehouse missing = new Warehouse();
    missing.businessUnitCode = "BU-MISSING";

    assertThrows(NotFoundException.class, () -> repository.update(missing));

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-EXISTING";
    w.location = "LOC";
    w.capacity = 10;
    w.stock = 1;

    repository.create(w);

    Warehouse updated = new Warehouse();
    updated.businessUnitCode = "BU-EXISTING";
    updated.location = "LOC-UPDATED";
    updated.capacity = 20;
    updated.stock = 5;

    repository.update(updated);

    Warehouse found = repository.findByBusinessUnitCode("BU-EXISTING");
    assertNotNull(found);
    assertEquals("LOC-UPDATED", found.location);
    assertEquals(20, found.capacity);
    assertEquals(5, found.stock);
  }

  @Test
  @Transactional
  void create_withNull_shouldReturn() {
    repository.create(null);
    // Should not throw, just log warning
  }

  @Test
  @Transactional
  void update_withNull_shouldThrow() {
    assertThrows(ValidationException.class, () -> repository.update(null));
  }

  @Test
  @Transactional
  void remove_withNull_shouldReturn() {
    repository.remove(null);
    // Should not throw, just log warning
  }

  @Test
  @Transactional
  void remove_nonExistent_shouldReturn() {
    Warehouse missing = new Warehouse();
    missing.businessUnitCode = "BU-NONEXIST";
    repository.remove(missing);
    // Should not throw, just log that nothing to delete
  }

  @Test
  @Transactional
  void remove_existing_shouldDelete() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-TO-DELETE";
    w.location = "LOC";
    w.capacity = 10;
    w.stock = 1;
    repository.create(w);

    Warehouse found = repository.findByBusinessUnitCode("BU-TO-DELETE");
    assertNotNull(found);

    repository.remove(w);

    Warehouse notFound = repository.findByBusinessUnitCode("BU-TO-DELETE");
    assertNull(notFound);
  }

  @Test
  @Transactional
  void findByBusinessUnitCode_withNull_shouldReturnNull() {
    Warehouse found = repository.findByBusinessUnitCode(null);
    assertNull(found);
  }

  @Test
  @Transactional
  void findByBusinessUnitCode_notExists_shouldReturnNull() {
    Warehouse found = repository.findByBusinessUnitCode("BU-NOTEXIST");
    assertNull(found);
  }

  @Test
  @Transactional
  void findByBusinessUnitCode_archived_shouldReturnNull() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-ARCHIVED";
    w.location = "LOC";
    w.capacity = 10;
    w.stock = 1;
    w.archivedAt = LocalDateTime.now();
    repository.create(w);

    Warehouse found = repository.findByBusinessUnitCode("BU-ARCHIVED");
    assertNull(found);
  }

  @Test
  @Transactional
  void countByLocation_withNull_shouldReturnZero() {
    long count = repository.countByLocation(null);
    assertEquals(0, count);
  }

  @Test
  @Transactional
  void countByLocation_withData_shouldReturnCorrectCount() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "BU-LOC1";
    w1.location = "TEST-LOC";
    w1.capacity = 10;
    w1.stock = 1;
    repository.create(w1);

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "BU-LOC2";
    w2.location = "TEST-LOC";
    w2.capacity = 20;
    w2.stock = 2;
    repository.create(w2);

    long count = repository.countByLocation("TEST-LOC");
    assertEquals(2, count);
  }

  @Test
  @Transactional
  void countByLocation_excludesArchived() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "BU-ACTIVE";
    w1.location = "TEST-LOC2";
    w1.capacity = 10;
    w1.stock = 1;
    repository.create(w1);

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "BU-ARCHIVED2";
    w2.location = "TEST-LOC2";
    w2.capacity = 20;
    w2.stock = 2;
    w2.archivedAt = LocalDateTime.now();
    repository.create(w2);

    long count = repository.countByLocation("TEST-LOC2");
    assertEquals(1, count);
  }

  @Test
  @Transactional
  void getTotalCapacityByLocation_withNull_shouldReturnZero() {
    int total = repository.getTotalCapacityByLocation(null);
    assertEquals(0, total);
  }

  @Test
  @Transactional
  void getTotalCapacityByLocation_withData_shouldReturnSum() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "BU-CAP1";
    w1.location = "CAP-LOC";
    w1.capacity = 10;
    w1.stock = 1;
    repository.create(w1);

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "BU-CAP2";
    w2.location = "CAP-LOC";
    w2.capacity = 20;
    w2.stock = 2;
    repository.create(w2);

    int total = repository.getTotalCapacityByLocation("CAP-LOC");
    assertEquals(30, total);
  }

  @Test
  @Transactional
  void getTotalCapacityByLocation_excludesArchived() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "BU-CAP-ACTIVE";
    w1.location = "CAP-LOC2";
    w1.capacity = 10;
    w1.stock = 1;
    repository.create(w1);

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "BU-CAP-ARCHIVED";
    w2.location = "CAP-LOC2";
    w2.capacity = 20;
    w2.stock = 2;
    w2.archivedAt = LocalDateTime.now();
    repository.create(w2);

    int total = repository.getTotalCapacityByLocation("CAP-LOC2");
    assertEquals(10, total);
  }

  @Test
  @Transactional
  void getAll_shouldReturnAllWarehouses() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "BU-ALL1";
    w1.location = "LOC1";
    w1.capacity = 10;
    w1.stock = 1;
    repository.create(w1);

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "BU-ALL2";
    w2.location = "LOC2";
    w2.capacity = 20;
    w2.stock = 2;
    repository.create(w2);

    var all = repository.getAll();
    assertTrue(all.size() >= 2);
  }
}
