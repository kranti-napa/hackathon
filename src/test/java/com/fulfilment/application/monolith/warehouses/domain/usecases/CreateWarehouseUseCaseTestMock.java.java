package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class CreateWarehouseUseCaseTestMock {

    private WarehouseStore warehouseStoreMock;
    private CreateWarehouseUseCase createUseCase;

    @BeforeEach
    void setup() {
        // Create a mock for WarehouseStore
        warehouseStoreMock = mock(WarehouseStore.class);

        // Inject the mock into our use case
        createUseCase = new CreateWarehouseUseCase(warehouseStoreMock);
    }

    @Test
    void create_shouldCallCreateOnWarehouseStore() {
        // Arrange
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH.001");
        warehouse.setLocation("Location A");
        warehouse.setCapacity(100);
        warehouse.setStock(10);

        // Act
        createUseCase.create(warehouse);

        // Assert
        verify(warehouseStoreMock, times(1)).create(warehouse);
    }
}
