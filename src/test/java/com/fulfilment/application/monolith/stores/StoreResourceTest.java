package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceTest {

    @InjectMock
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    // ---------- helpers ----------

    private Long createStore(String name, int qty) {
        Store store = new Store();
        store.name = name;
        store.quantityProductsInStock = qty;

        Response response =
            given()
                .contentType(ContentType.JSON)
                .body(store)
            .when()
                .post("/store")
            .then()
                .statusCode(201)
                .extract().response();

        return response.jsonPath().getLong("id");
    }

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
    void shouldUpdateStoreSuccessfully() {
        Long id = createStore("Old", 5);

        Store updated = new Store();
        updated.name = "Updated";
        updated.quantityProductsInStock = 20;

        given()
            .contentType(ContentType.JSON)
            .body(updated)
        .when()
            .put("/store/" + id)
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated"))
            .body("quantityProductsInStock", equalTo(20)
