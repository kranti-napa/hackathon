package com.fulfilment.application.monolith.fulfilment.domain.ports;

import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentAssignment;
import java.util.List;

public interface FulfilmentAssignmentStore {

    List<FulfilmentAssignment> getAll();

    void create(FulfilmentAssignment assignment);
}
