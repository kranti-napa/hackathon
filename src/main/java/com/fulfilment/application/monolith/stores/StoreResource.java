package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Synchronization;
import jakarta.ws.rs.PathParam;
import jakarta.transaction.Status;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Inject
  TransactionSynchronizationRegistry txRegistry;
  
  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(@PathParam("id") Long id) {
    LOGGER.debugf("getSingle called with id=%s", id);
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Store with id of " + id + " does not exist.").build());
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    LOGGER.debugf("create called for store=%s", store == null ? "<null>" : store.name);
    if (store.id != null) {
  throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Id was invalidly set on request.").build());
    }

    store.persist();

    // Schedule legacy side-effect after the transaction commits
    txRegistry.registerInterposedSynchronization(new Synchronization() {

      @Override
      public void beforeCompletion() {
        // no-op
      }

      @Override
      public void afterCompletion(int status) {
        if (status == Status.STATUS_COMMITTED) {
          legacyStoreManagerGateway.createStoreOnLegacySystem(store);
        }
      }
    });

    return Response.status(Response.Status.CREATED).entity(store).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(@PathParam("id") Long id, Store updatedStore) {
    LOGGER.debugf("update called for id=%s newName=%s", id, updatedStore == null ? "<null>" : updatedStore.name);
    if (updatedStore == null || updatedStore.name == null) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Store Name was not set on request.").build());
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Store with id of " + id + " does not exist.").build());
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

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(@PathParam("id") Long id, Store updatedStore) {
    LOGGER.debugf("patch called for id=%s", id);
    if (updatedStore == null) {
      throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Request body was empty.").build());
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Store with id of " + id + " does not exist.").build());
    }

    // apply partial updates only when the incoming values are provided
    if (updatedStore.name != null) {
      entity.name = updatedStore.name;
    }

    // update quantity only when caller provided a non-zero value
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

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") Long id) {
    LOGGER.debugf("delete called for id=%s", id);
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Store with id of " + id + " does not exist.").build());
    }
    entity.delete();
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
