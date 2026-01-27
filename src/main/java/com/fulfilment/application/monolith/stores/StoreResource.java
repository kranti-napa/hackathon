package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;

@Path(StoreResource.PATH_STORE)
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StoreResource {

    static final String PATH_STORE = "store";
    private static final String PATH_ID = "{id}";
    private static final String PARAM_ID = "id";
    private static final String SORT_NAME = "name";
    private static final String LOG_GET_SINGLE = "getSingle called with id=%s";
    private static final String LOG_CREATE = "create called";
    private static final String LOG_UPDATE = "update called for id=%s";
    private static final String LOG_PATCH = "patch called for id=%s";
    private static final String LOG_DELETE = "delete called for id=%s";
    private static final String JSON_EXCEPTION_TYPE = "exceptionType";
    private static final String JSON_CODE = "code";
    private static final String JSON_ERROR = "error";

    private static final Logger LOGGER = Logger.getLogger(StoreResource.class);

    @Inject
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    @Inject
    TransactionSynchronizationRegistry txRegistry;

    @Inject
    EntityManager entityManager;

    // ---------- GET ALL ----------
    @GET
    public List<Store> get() {
        return Store.listAll(Sort.by(SORT_NAME));
    }

    // ---------- GET BY ID ----------
    @GET
    @Path(PATH_ID)
    public Store getSingle(@PathParam(PARAM_ID) Long id) {
        LOGGER.debugf(LOG_GET_SINGLE, id);

        if (id == null) {
            throw new ValidationException(AppConstants.ERR_STORE_ID_REQUIRED);
        }

        Store entity = entityManager.find(Store.class, id);
        if (entity == null) {
            throw new NotFoundException(
                    String.format(AppConstants.ERR_STORE_NOT_FOUND, id)
            );
        }
        return entity;
    }

    // ---------- CREATE ----------
    @POST
    @Transactional
    public Response create(Store store) {
        LOGGER.debug(LOG_CREATE);

        if (store == null) {
            throw new ValidationException(AppConstants.ERR_STORE_NULL);
        }

        if (store.id != null) {
            throw new ConflictException(AppConstants.ERR_STORE_ID_NOT_ALLOWED);
        }

        store.persist();

        txRegistry.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {}

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    legacyStoreManagerGateway.createStoreOnLegacySystem(store);
                }
            }
        });

        return Response.status(Response.Status.CREATED).entity(store).build();
    }

    // ---------- UPDATE ----------
    @PUT
    @Path(PATH_ID)
    @Transactional
    public Store update(@PathParam(PARAM_ID) Long id, Store updatedStore) {
        LOGGER.debugf(LOG_UPDATE, id);

        if (id == null) {
            throw new ValidationException(AppConstants.ERR_STORE_ID_REQUIRED);
        }

        if (updatedStore == null || updatedStore.name == null) {
            throw new ValidationException(AppConstants.ERR_STORE_NULL);
        }

        if (updatedStore.id != null && !updatedStore.id.equals(id)) {
            throw new ValidationException(AppConstants.ERR_STORE_ID_MISMATCH);
        }

        Store entity = Store.findById(id);
        if (entity == null) {
            throw new NotFoundException(
                    String.format(AppConstants.ERR_STORE_NOT_FOUND, id)
            );
        }

        entity.name = updatedStore.name;
        entity.quantityProductsInStock = updatedStore.quantityProductsInStock;

        txRegistry.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {}

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    legacyStoreManagerGateway.updateStoreOnLegacySystem(entity);
                }
            }
        });

        return entity;
    }

    // ---------- PATCH ----------
    @PATCH
    @Path(PATH_ID)
    @Transactional
    public Store patch(@PathParam(PARAM_ID) Long id, Store updatedStore) {
        LOGGER.debugf(LOG_PATCH, id);

        if (id == null) {
            throw new ValidationException(AppConstants.ERR_STORE_ID_REQUIRED);
        }

        if (updatedStore == null) {
            throw new ValidationException(AppConstants.ERR_STORE_NULL);
        }

        if (updatedStore.id != null && !updatedStore.id.equals(id)) {
            throw new ValidationException(AppConstants.ERR_STORE_ID_MISMATCH);
        }

        Store entity = Store.findById(id);
        if (entity == null) {
            throw new NotFoundException(
                    String.format(AppConstants.ERR_STORE_NOT_FOUND, id)
            );
        }

        if (updatedStore.name != null) {
            entity.name = updatedStore.name;
        }

        if (updatedStore.quantityProductsInStock != 0) {
            entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
        }

        txRegistry.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {}

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    legacyStoreManagerGateway.updateStoreOnLegacySystem(entity);
                }
            }
        });

        return entity;
    }

    // ---------- DELETE ----------
    @DELETE
    @Path(PATH_ID)
    @Transactional
    public Response delete(@PathParam(PARAM_ID) Long id) {
        LOGGER.debugf(LOG_DELETE, id);

        if (id == null) {
            throw new ValidationException(AppConstants.ERR_STORE_ID_REQUIRED);
        }

        Store entity = Store.findById(id);
        if (entity == null) {
            throw new NotFoundException(
                    String.format(AppConstants.ERR_STORE_NOT_FOUND, id)
            );
        }

        entity.delete();
        entityManager.flush();
        entityManager.clear();

        return Response.noContent().build();
    }

    // ---------- ERROR MAPPER (kept for backward compatibility in tests) ----------
    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Response toResponse(Exception exception) {

            int code = 500;
            if (exception instanceof ValidationException) {
                code = 400;
            } else if (exception instanceof ConflictException) {
                code = 409;
            } else if (exception instanceof NotFoundException) {
                code = 404;
            }

            ObjectNode json = objectMapper.createObjectNode();
            json.put(JSON_EXCEPTION_TYPE, exception.getClass().getName());
            json.put(JSON_CODE, code);
            json.put(JSON_ERROR, exception.getMessage());

            return Response.status(code).entity(json).build();
        }
    }
}
