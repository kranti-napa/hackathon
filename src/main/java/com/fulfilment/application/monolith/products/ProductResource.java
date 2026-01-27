package com.fulfilment.application.monolith.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.common.AppConstants;
import com.fulfilment.application.monolith.common.exceptions.ConflictException;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import com.fulfilment.application.monolith.common.exceptions.ValidationException;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

@Path(ProductResource.PATH_PRODUCT)
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

  @Inject ProductRepository productRepository;

  public static final String PATH_PRODUCT = "product";
  private static final String PATH_ID = "{id}";
  private static final String PARAM_ID = "id";
  private static final String SORT_NAME = "name";
  private static final String LOG_REQUEST_FAILED = "Failed to handle request";
  private static final String JSON_EXCEPTION_TYPE = "exceptionType";
  private static final String JSON_CODE = "code";
  private static final String JSON_ERROR = "error";

  private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());

  @GET
  public List<Product> get() {
    return productRepository.listAll(Sort.by(SORT_NAME));
  }

  @GET
  @Path(PATH_ID)
  public Product getSingle(@PathParam(PARAM_ID) Long id) {
    if (id == null) {
      throw new ValidationException(AppConstants.ERR_PRODUCT_ID_REQUIRED);
    }

    Product entity = productRepository.findById(id);
    if (entity == null) {
      throw new NotFoundException(String.format(AppConstants.ERR_PRODUCT_NOT_FOUND, id));
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Product product) {
    if (product == null) {
      throw new ValidationException(AppConstants.ERR_PRODUCT_NULL);
    }

    if (product.id != null) {
      throw new ConflictException(AppConstants.ERR_PRODUCT_ID_NOT_ALLOWED);
    }

    productRepository.persist(product);
    return Response.ok(product).status(201).build();
  }

  @PUT
  @Path(PATH_ID)
  @Transactional
  public Product update(@PathParam(PARAM_ID) Long id, Product product) {
    if (id == null) {
      throw new ValidationException(AppConstants.ERR_PRODUCT_ID_REQUIRED);
    }

    if (product == null || product.name == null) {
      throw new ValidationException(AppConstants.ERR_PRODUCT_NAME_REQUIRED);
    }

    if (product.id != null && !product.id.equals(id)) {
      throw new ValidationException(AppConstants.ERR_PRODUCT_ID_MISMATCH);
    }

    Product entity = productRepository.findById(id);

    if (entity == null) {
      throw new NotFoundException(String.format(AppConstants.ERR_PRODUCT_NOT_FOUND, id));
    }

    entity.name = product.name;
    entity.description = product.description;
    entity.price = product.price;
    entity.stock = product.stock;

    productRepository.persist(entity);

    return entity;
  }

  @DELETE
  @Path(PATH_ID)
  @Transactional
  public Response delete(@PathParam(PARAM_ID) Long id) {
    if (id == null) {
      throw new ValidationException(AppConstants.ERR_PRODUCT_ID_REQUIRED);
    }

    Product entity = productRepository.findById(id);
    if (entity == null) {
      throw new NotFoundException(String.format(AppConstants.ERR_PRODUCT_NOT_FOUND, id));
    }
    productRepository.delete(entity);
    return Response.status(204).build();
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error(LOG_REQUEST_FAILED, exception);

      int code = 500;
      if (exception instanceof ValidationException) {
        code = 400;
      } else if (exception instanceof ConflictException) {
        code = 409;
      } else if (exception instanceof NotFoundException) {
        code = 404;
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put(JSON_EXCEPTION_TYPE, exception.getClass().getName());
      exceptionJson.put(JSON_CODE, code);

      if (exception.getMessage() != null) {
        exceptionJson.put(JSON_ERROR, exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
