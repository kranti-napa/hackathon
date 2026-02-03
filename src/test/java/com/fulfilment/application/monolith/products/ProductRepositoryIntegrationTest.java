package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ProductRepositoryIntegrationTest {

    @Inject
    ProductRepository productRepository;

    @BeforeEach
    @Transactional
    public void cleanup() {
        productRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testCreateAndFindProduct() {
        Product product = new Product("Integration Test Product");
        product.description = "Test Description";
        product.price = new BigDecimal("49.99");
        product.stock = 50;

        productRepository.persist(product);
        assertNotNull(product.id);

        Product found = productRepository.findById(product.id);
        assertNotNull(found);
        assertEquals("Integration Test Product", found.name);
        assertEquals("Test Description", found.description);
        assertEquals(new BigDecimal("49.99"), found.price);
        assertEquals(50, found.stock);
    }

    @Test
    @Transactional
    public void testUpdateProduct() {
        Product product = new Product("Update Test");
        product.stock = 10;
        productRepository.persist(product);

        product.stock = 20;
        product.description = "Updated";
        productRepository.persist(product);

        Product updated = productRepository.findById(product.id);
        assertEquals(20, updated.stock);
        assertEquals("Updated", updated.description);
    }

    @Test
    @Transactional
    public void testDeleteProduct() {
        Product product = new Product("Delete Test");
        productRepository.persist(product);
        Long id = product.id;

        productRepository.delete(product);

        Product deleted = productRepository.findById(id);
        assertNull(deleted);
    }

    @Test
    @Transactional
    public void testFindAll() {
        Product p1 = new Product("Product 1");
        Product p2 = new Product("Product 2");
        Product p3 = new Product("Product 3");

        productRepository.persist(p1);
        productRepository.persist(p2);
        productRepository.persist(p3);

        List<Product> all = productRepository.listAll();
        assertEquals(3, all.size());
    }

    @Test
    @Transactional
    public void testCount() {
        assertEquals(0, productRepository.count());

        Product product = new Product("Count Test");
        productRepository.persist(product);

        assertEquals(1, productRepository.count());
    }
}
