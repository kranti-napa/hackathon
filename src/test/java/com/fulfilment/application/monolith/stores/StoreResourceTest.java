package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
class StoreResourceTest {

    @Inject
    EntityManager entityManager;

    @Inject
    StoreResource storeResource;

    @InjectMock
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    // ---------- REST ENDPOINT TESTS ----------

    @Test
    void shouldReturnEmptyStoreListInitially() {
        given()
            .when().get("/store")
            .then()
            .statusCode(200)
            .body("name", hasItems("TONSTAD", "KALLAX", "BESTÅ"));
    }

    @Test
    void shouldReturn404WhenStoreNotFoundById() {
        given()
            .when().get("/store/99999")
            .then()
            .statusCode(404)
            .body("error", containsString(String.format(AppConstants.ERR_STORE_NOT_FOUND, 99999)));
    }

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
    void shouldReturnConflictWhenIdIsProvidedOnCreate() {
        Store store = new Store();
        store.id = 1L;
        store.name = "Invalid Store";

        given()
            .contentType(ContentType.JSON)
            .body(store)
        .when()
            .post("/store")
        .then()
            .statusCode(409)
            .body("error", containsString(AppConstants.ERR_STORE_ID_NOT_ALLOWED));
    }

    @Test
    void shouldUpdateStoreSuccessfully() {
        Long storeId = createStoreViaApi("Old Name", 5);

        Store updated = new Store();
        updated.name = "New Name";
        updated.quantityProductsInStock = 20;

        given()
            .contentType(ContentType.JSON)
            .body(updated)
        .when()
            .put("/store/" + storeId)
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
            .body("error", containsString(AppConstants.ERR_STORE_NULL));
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

    @Test
    void shouldPatchOnlyName() {
        Long storeId = createStoreViaApi("Initial", 50);

        Store patch = new Store();
        patch.name = "Patched";

        given()
            .contentType(ContentType.JSON)
            .body(patch)
        .when()
            .patch("/store/" + storeId)
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
            .body("error", containsString(AppConstants.ERR_STORE_NULL));
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

    @Test
    void shouldDeleteStoreSuccessfully() {
        Long storeId = createStoreViaApi("To Delete", 1);

        given()
            .when()
            .delete("/store/" + storeId)
        .then()
            .statusCode(204);

        given()
            .when()
            .get("/store/" + storeId)
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

    // ---------- RESOURCE BEHAVIOR TESTS ----------

    @Test
    void createGetDeleteFlow() {
        Store store = new Store();
        store.name = "TestStoreIT";
        store.quantityProductsInStock = 1;

        Long id =
            given()
                .contentType(ContentType.JSON)
                .body(store)
            .when()
                .post("/store")
            .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        given()
            .when()
            .get("/store/" + id)
        .then()
            .statusCode(200);

        given()
            .when()
            .delete("/store/" + id)
        .then()
            .statusCode(204);

        given()
            .when()
            .get("/store/" + id)
        .then()
            .statusCode(404);
    }

    @Test
    void updateAndPatchFlowWithQuantities() {
        Store s = new Store("patchNonZero");
        s.quantityProductsInStock = 10;
        Response resp = storeResource.create(s);
        Store created = (Store) resp.getEntity();

        Store updated = new Store();
        updated.name = "patched2";
        updated.quantityProductsInStock = 20;

        Store res = storeResource.patch(created.id, updated);
        assertEquals("patched2", res.name);
        assertEquals(20, res.quantityProductsInStock);

        storeResource.delete(created.id);
    }

    // ---------- ERROR MAPPER TESTS ----------

    @Test
    void errorMapper_mapsNotFoundValidationConflict() throws Exception {
        StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();

        Field omField = StoreResource.ErrorMapper.class.getDeclaredField("objectMapper");
        omField.setAccessible(true);
        omField.set(mapper, new ObjectMapper());

        Response notFound = mapper.toResponse(
                new NotFoundException(String.format(AppConstants.ERR_STORE_NOT_FOUND, 1))
        );
        assertEquals(404, notFound.getStatus());

        Response validation = mapper.toResponse(
                new ValidationException(AppConstants.ERR_STORE_NULL)
        );
        assertEquals(400, validation.getStatus());

        Response conflict = mapper.toResponse(
                new ConflictException(AppConstants.ERR_STORE_ID_NOT_ALLOWED)
        );
        assertEquals(409, conflict.getStatus());

        ObjectNode node = (ObjectNode) notFound.getEntity();
        assertTrue(node.has("exceptionType"));
        assertTrue(node.has("code"));
        assertTrue(node.has("error"));
    }

    // ---------- UNIT-STYLE TESTS WITH MOCKS ----------

    @Test
    void getSingle_notFound_throwsNotFound() {
        EntityManager em = mock(EntityManager.class);
        when(em.find(Store.class, 42L)).thenReturn(null);

        StoreResource resource = newResourceWithMocks(em, mock(TransactionSynchronizationRegistry.class), mock(LegacyStoreManagerGateway.class));

        assertThrows(NotFoundException.class, () -> resource.getSingle(42L));
    }

    @Test
    void create_withId_throwsConflict() {
        Store s = new Store();
        s.id = 123L;

        StoreResource resource = newResourceWithMocks(mock(EntityManager.class), mock(TransactionSynchronizationRegistry.class), mock(LegacyStoreManagerGateway.class));

        assertThrows(ConflictException.class, () -> resource.create(s));
    }

    @Test
    void create_registersPostCommit_and_afterCommitInvokesLegacy() {
        TransactionSynchronizationRegistry txRegistry = mock(TransactionSynchronizationRegistry.class);
        LegacyStoreManagerGateway legacy = mock(LegacyStoreManagerGateway.class);

        StoreResource resource = newResourceWithMocks(mock(EntityManager.class), txRegistry, legacy);

        Store s = new Store() {
            @Override
            public void persist() {
                // no-op for unit test
            }
        };

        ArgumentCaptor<Synchronization> captor = ArgumentCaptor.forClass(Synchronization.class);

        resource.create(s);

        verify(txRegistry).registerInterposedSynchronization(captor.capture());
        Synchronization sync = captor.getValue();
        sync.afterCompletion(Status.STATUS_COMMITTED);

        verify(legacy).createStoreOnLegacySystem(s);
    }

    @Test
    void update_nullName_throwsValidation() {
        Store updated = new Store();
        updated.name = null;

        StoreResource resource = newResourceWithMocks(mock(EntityManager.class), mock(TransactionSynchronizationRegistry.class), mock(LegacyStoreManagerGateway.class));

        assertThrows(ValidationException.class, () -> resource.update(1L, updated));
    }

    @Test
    void patch_nullBody_throwsValidation() {
        StoreResource resource = newResourceWithMocks(mock(EntityManager.class), mock(TransactionSynchronizationRegistry.class), mock(LegacyStoreManagerGateway.class));

        assertThrows(ValidationException.class, () -> resource.patch(1L, null));
    }

    private Store persistStore(String name, int qty) {
        Store store = new Store();
        store.name = name;
        store.quantityProductsInStock = qty;
        entityManager.persist(store);
        entityManager.flush();
        entityManager.clear();
        return store;
    }

    private StoreResource newResourceWithMocks(EntityManager em,
                                               TransactionSynchronizationRegistry txRegistry,
                                               LegacyStoreManagerGateway legacy) {
        StoreResource resource = new StoreResource();
        resource.entityManager = em;
        resource.txRegistry = txRegistry;
        resource.legacyStoreManagerGateway = legacy;
        return resource;
    }

    private Long createStoreViaApi(String name, int qty) {
        Store store = new Store();
        store.name = name;
        store.quantityProductsInStock = qty;

        return given()
            .contentType(ContentType.JSON)
            .body(store)
        .when()
            .post("/store")
        .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");
    }
}
