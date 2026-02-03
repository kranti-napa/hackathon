package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentAssignmentStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Default
@Priority(1)
@ApplicationScoped
@Transactional
public class FulfilmentAssignmentRepository implements FulfilmentAssignmentStore, PanacheRepository<DbFulfilmentAssignment> {

    private static final Logger LOGGER = Logger.getLogger(FulfilmentAssignmentRepository.class);

    @Override
    public List<FulfilmentAssignment> getAll() {
        return listAll().stream()
            .map(DbFulfilmentAssignment::toFulfilmentAssignment)
            .collect(Collectors.toList());
    }

    @Override
    public void create(FulfilmentAssignment assignment) {
        if (assignment == null) {
            LOGGER.warn("create called with null assignment");
            return;
        }

        DbFulfilmentAssignment dbEntity = DbFulfilmentAssignment.fromFulfilmentAssignment(assignment);
        persist(dbEntity);
        LOGGER.debugf("Created fulfilment assignment: store=%s, product=%s, warehouse=%s",
            assignment.storeId, assignment.productId, assignment.warehouseBusinessUnitCode);
    }
}
