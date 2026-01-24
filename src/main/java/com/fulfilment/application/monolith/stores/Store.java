package com.fulfilment.application.monolith.stores;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
// Note: removed second-level caching to avoid stale read-after-delete in tests
// (L2 cache could return deleted entities in some test scenarios).
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Store extends PanacheEntity {

  @Column(length = 40, unique = true)
  public String name;

  public int quantityProductsInStock;

  public Store() {}

  public Store(String name) {
    this.name = name;
  }
}
