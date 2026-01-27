package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.hibernate.query.NativeQuery;
import jakarta.persistence.EntityManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import com.fulfilment.application.monolith.common.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

@QuarkusTest
public class StoreResourceMockedTest {

  @Inject
  StoreResource resource;

  @InjectMock
  EntityManager entityManager;

  @InjectMock
  TransactionSynchronizationRegistry txRegistry;

  @Test
  public void getSingle_whenNativeCountThrows_fallsBackToFind() {
    Long id = 1234L;

    // make createNativeQuery(...) throw to force fallback
    when(entityManager.createNativeQuery(anyString())).thenThrow(new RuntimeException("native-err"));

    // set up entityManager.find to return a Store instance
    Store s = new Store("mocked");
    s.id = id;
    when(entityManager.find(Store.class, id)).thenReturn(s);

    Store result = resource.getSingle(id);
    Assertions.assertNotNull(result);
    Assertions.assertEquals(id, result.id);
  }

  @Test
  public void getSingle_whenNativeCountZero_throwsNotFound() {
    Long id = 9999L;

  // mock NativeQuery to return zero count (matches runtime EM return type)
  NativeQuery q = mock(NativeQuery.class);
  when(entityManager.createNativeQuery(anyString())).thenReturn(q);
  when(q.setParameter(eq(1), eq(id))).thenReturn(q);
  when(q.getSingleResult()).thenReturn(0L);

    Assertions.assertThrows(NotFoundException.class, () -> resource.getSingle(id));
  }

  @Test
  public void create_registersTransactionSynchronization() {
    Store s = new Store("tx-store");

    resource.create(s);

    // verify that a synchronization was registered
    verify(txRegistry, atLeastOnce()).registerInterposedSynchronization(any());
  }
}
