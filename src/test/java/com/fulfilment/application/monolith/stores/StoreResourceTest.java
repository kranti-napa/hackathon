package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceTest {

    @Inject
    EntityManager entityManager;

    @InjectMock
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    // ---------- GET ----------

    @Test
    void shouldReturnStores() {
        given()
            .when().get("/store")
            .then()
            .statusCode(200)
            .body("$", not(empty()));
    }

    @Test
    void shouldReturn404WhenStoreNotFoundById() {
        given()
            .when().get("/store/999999")
            .then()
            .statusCode(404)
            .body(containsString("does not exist"));
    }

    // ---------- POST ----------

    @Test
    @Transactional
    void shouldCreateStoreSuccessfully() {
        Store store = new Store();
        store.name = "Created Store";
        store.quantityProductsInStock = 10;

        given()
            .contentType(ContentType.JSON)
            .body(store)
        .when()
            .post("/store")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Created Store"));
    }

    @Test
    void shouldReturnBadRequestWhenIdIsProvidedOnCreate() {
        Store store = new Store();
        store.id = 1L;
        store.name = "Invalid";

        given()
            .contentType(ContentType.JSON)
            .body(store)
        .when()
            .post("/store")
        .then()
            .statusCode(400)
            .body(containsString("Id was invalidly set"));
    }

    // ---------- PUT ----------

    @Test
    @Transactional
    void shouldUpdateStoreSuccessfully() {
        Store store = persistStore("Old", 5);

        Store updated = new Store();
        updated.name = "Updated";
        updated.quantityProductsInStock = 20;

        given()
            .contentType(ContentType.JSON)
            .body(updated)
        .when()
            .put("/store/" + store.id)
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated"))
            .body("quantityProductsInStock", equalTo(20));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithNullBody() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .put("/store/1")
        .then()
            .statusCode(400)
            .body(containsString("Store Name was not set"));
    }

    // ---------- PATCH ----------

    @Test
    @Transactional
    void shouldPatchOnlyName() {
        Store store = persistStore("Initial", 50);

        Store patch = new Store();
        patch.name = "Patched";

        given()
            .contentType(ContentType.JSON)
            .body(patch)
        .when()
            .patch("/store/" + store.id)
        .then()
            .statusCode(200)
            .body("name", equalTo("Patched"))
            .body("quantityProductsInStock", equalTo(50));
    }

    @Test
    void shouldReturnBadRequestWhenPatchBodyIsNull() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .patch("/store/1")
        .then()
            .statusCode(400)
            .body(containsString("Request body was empty"));
    }

    // ---------- DELETE ----------

    @Test
    @Transactional
    void shouldDeleteStoreSuccessfully() {
        Store store = persistStore("Delete Me", 1);

        given()
            .when()
            .delete("/store/" + store.id)
        .then()
            .statusCode(204);

        given()
            .when()
            .get("/store/" + store.id)
        .then()
            .statusCode(404);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingStore() {
        given()
            .when()
            .delete("/store/999999")
        .then()
            .statusCode(404);
    }

    // ---------- helper ----------

    private Store persistStore(String name, int qty) {
        Store store = new Store();
        store.name = name;
        store.quantityProductsInStock = qty;
        entityManager.persist(store);
        entityManager.flush();
        entityManager.clear();
        return store;
    }
}
