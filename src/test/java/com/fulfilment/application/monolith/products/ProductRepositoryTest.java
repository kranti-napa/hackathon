package com.fulfilment.application.monolith.products;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductRepositoryTest {

    @Test
    public void testRepositoryInstantiation() {
        ProductRepository repository = new ProductRepository();
        assertNotNull(repository);
    }
}
