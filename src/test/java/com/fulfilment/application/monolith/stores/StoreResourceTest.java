package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
class StoreResourceTest {

    @Test
    void testPatchStore() {
        // Step 1: Create a store
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(new Store(null, "Patch Me", 1))
                        .when()
                        .post("/store")
                        .then()
                        .statusCode(201)
                        .extract()
                        .response();

        // Step 2: Extract the actual ID
        Long id = response.jsonPath().getLong("id");

        // Step 3: Prepare updated store
        Store updated = new Store();
        updated.name = "Patched";
        updated.quantityProductsInStock = 99;

        // Step 4: PATCH using the correct ID
        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .patch("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", is("Patched"))
                .body("quantityProductsInStock", is(99));
    }

    @Test
    void testCreateStore() {
        Store store = new Store();
        store.name = "Main Store";
        store.quantityProductsInStock = 10;

        given()
                .contentType(ContentType.JSON)
                .body(store)
                .when()
                .post("/store")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", is("Main Store"));
    }

    @Test
    void testCreateStoreWithIdShouldFail() {
        Store store = new Store();
        store.id = 1L;
        store.name = "Invalid Store";

        given()
                .contentType(ContentType.JSON)
                .body(store)
                .when()
                .post("/store")
                .then()
                .statusCode(422);
    }

    @Test
    void testGetAllStores() {
        // create via REST to ensure committed data
        Store store = new Store();
        store.name = "Store A";

        given()
                .contentType(ContentType.JSON)
                .body(store)
                .when()
                .post("/store")
                .then()
                .statusCode(201);

        given()
                .when()
                .get("/store")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void testGetSingleStoreNotFound() {
        given()
                .when()
                .get("/store/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void testUpdateStore() {
        // Create store via POST and extract id safely
        Long id =
                given()
                        .contentType(ContentType.JSON)
                        .body(new Store(null, "Old Name", 3))
                        .when()
                        .post("/store")
                        .then()
                        .statusCode(201)
                        .extract()
                        .response()
                        .jsonPath()
                        .getLong("id"); // ✅ safe extraction

        // Prepare updated store
        Store updated = new Store();
        updated.name = "New Name";
        updated.quantityProductsInStock = 15;

        // Perform update
        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                .put("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", is("New Name"))
                .body("quantityProductsInStock", is(15));
    }


    @Test
    void testDeleteStore() {
        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(new Store(null, "Delete Me", 1))
                        .when()
                        .post("/store")
                        .then()
                        .statusCode(201)
                        .extract()
                        .response();

// Extract id safely
        Long id = response.jsonPath().getLong("id");

// Now use it in DELETE
        given()
                .when()
                .delete("/store/" + id)
                .then()
                .statusCode(204);
    }
}
