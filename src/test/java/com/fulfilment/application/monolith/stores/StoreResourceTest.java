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
    void shouldReturnEmptyStoreListInitially() {
        given()
            .when().get("/store")
            .then()
            .statusCode(200)
            .body("$", is(empty()));
    }

    @Test
    void shouldReturn404WhenStoreNotFoundById() {
        given()
            .when().get("/store/99999")
            .then()
            .statusCode(404)
            .body("error", containsString("does not exist"));
    }

    // ---------- POST ----------

    @Test
    void shouldCreateStoreSuccessfully() {
        Store store = new Store();
        store.name = "My Store";
        store.quantityProductsInStock = 10;

        given()
            .contentType(ContentType.JSON)
            .body(store)
        .when()
            .post("/store")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("My Store"));
    }

    @Test
    void shouldReturnBadRequestWhenIdIsProvidedOnCreate() {
        Store store = new Store();
        store.id = 1L;
        store.name = "Invalid Store";

        given()
            .contentType(ContentType.JSON)
            .body(store)
        .when()
            .post("/store")
        .then()
            .statusCode(400)
            .body("error", containsString("Id was invalidly set"));
    }

    // ---------- PUT ----------

    @Test
    @Transactional
    void shouldUpdateStoreSuccessfully() {
        Store store = persistStore("Old Name", 5);

        Store updated = new Store();
        updated.name = "New Name";
        updated.quantityProductsInStock = 20;

        given()
            .contentType(ContentType.JSON)
            .body(updated)
        .when()
            .put("/store/" + store.id)
        .then()
            .statusCode(200)
            .body("name", equalTo("New Name"))
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
            .body("error", containsString("Store Name was not set"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingStore() {
        Store updated = new Store();
        updated.name = "Name";

        given()
            .contentType(ContentType.JSON)
            .body(updated)
        .when()
            .put("/store/9999")
        .then()
            .statusCode(404);
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
            .body("error", containsString("Request body was empty"));
    }

    @Test
    void shouldReturn404WhenPatchingNonExistingStore() {
        Store patch = new Store();
        patch.name = "X";

        given()
            .contentType(ContentType.JSON)
            .body(patch)
        .when()
            .patch("/store/9999")
        .then()
            .statusCode(404);
    }

    // ---------- DELETE ----------

    @Test
    @Transactional
    void shouldDeleteStoreSuccessfully() {
        Store store = persistStore("To Delete", 1);

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
            .delete("/store/9999")
        .then()
            .statusCode(404);
    }

    // ---------- helpers ----------

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
