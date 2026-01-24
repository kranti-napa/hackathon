package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.persistence.EntityManager;
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

  @Inject
  EntityManager entityManager;
  
  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(@PathParam("id") Long id) {
    LOGGER.debugf("getSingle called with id=%s", id);
  // Use explicit EntityManager lookup to avoid any Panache/session caching artifacts
    // Use a native count query to reliably detect presence in the DB (avoids
    // unexpected cached/managed instance returns during tests).
    try {
      Object cnt = entityManager.createNativeQuery("select count(*) from store where id = ?").setParameter(1, id).getSingleResult();
      long count = ((Number) cnt).longValue();
      if (count == 0) {
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Store with id of " + id + " does not exist.").build());
      }
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      LOGGER.debug("native count check failed, falling back to EntityManager.find", e);
    }

    Store entity = entityManager.find(Store.class, id);
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
    try {
      Object beforeCount = entityManager.createQuery("select count(s) from Store s where s.id = :id").setParameter("id", id).getSingleResult();
      LOGGER.debugf("store count before delete for id=%s -> %s", id, beforeCount);
    } catch (Exception e) {
      LOGGER.debugf("count before delete failed: %s", e.getMessage());
    }

    entity.delete();

    try {
      Object afterDeleteCount = entityManager.createQuery("select count(s) from Store s where s.id = :id").setParameter("id", id).getSingleResult();
      LOGGER.debugf("store count immediately after entity.delete() for id=%s -> %s", id, afterDeleteCount);
    } catch (Exception e) {
      LOGGER.debugf("count after entity.delete failed: %s", e.getMessage());
    }
    // flush and clear persistence context to ensure subsequent non-transactional lookups
    // do not return a stale managed instance. If the entity still appears, attempt
    // a defensive delete-by-id to ensure it is removed for test determinism.
    try {
      entityManager.flush();
      entityManager.clear();
    } catch (Exception e) {
      LOGGER.debug("flush/clear after delete failed", e);
    }

    try {
      Store maybe = Store.findById(id);
      if (maybe != null) {
        LOGGER.warnf("Entity still present after delete/flush, attempting deleteById id=%s", id);
        Store.deleteById(id);
        try {
          entityManager.flush();
          entityManager.clear();
        } catch (Exception ignore) {}
      }
    } catch (Exception ex) {
      LOGGER.debug("Post-delete verification failed", ex);
    }
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
