package com.fulfilment.application.monolith.fulfilment.adapters.inmemory;

import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentAssignmentStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class InMemoryFulfilmentAssignmentStore implements FulfilmentAssignmentStore {

    private final List<FulfilmentAssignment> assignments = new ArrayList<>();

    @Override
    public List<FulfilmentAssignment> getAll() {
        return Collections.unmodifiableList(assignments);
    }

    @Override
    public void create(FulfilmentAssignment assignment) {
        assignments.add(assignment);
    }
}
