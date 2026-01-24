package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Synchronization;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class StoreResourceUnitTest {

  StoreResource resource;
  EntityManager em;
  TransactionSynchronizationRegistry txRegistry;
  LegacyStoreManagerGateway legacy;

  @BeforeEach
  public void setup() {
    resource = new StoreResource();
    em = mock(EntityManager.class);
    txRegistry = mock(TransactionSynchronizationRegistry.class);
    legacy = mock(LegacyStoreManagerGateway.class);

    resource.entityManager = em;
    resource.txRegistry = txRegistry;
    resource.legacyStoreManagerGateway = legacy;
  }

  @Test
  public void getSingle_nativeCount_zero_throwsNotFound() {
    Query q = mock(Query.class);
    when(em.createNativeQuery(any(String.class))).thenReturn(q);
    when(q.setParameter(anyInt(), any())).thenReturn(q);
    when(q.getSingleResult()).thenReturn(0L);

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.getSingle(42L));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  public void getSingle_nativeQueryFails_fallbackToFind_entityMissing_throwsNotFound() {
    when(em.createNativeQuery(any(String.class))).thenThrow(new RuntimeException("boom"));
    when(em.find(Store.class, 1L)).thenReturn(null);

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.getSingle(1L));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  public void getSingle_nativeQueryFails_fallbackToFind_returnsEntity() {
    when(em.createNativeQuery(any(String.class))).thenThrow(new RuntimeException("boom"));
    Store s = mock(Store.class);
    when(em.find(Store.class, 7L)).thenReturn(s);

    Store out = resource.getSingle(7L);
    assertSame(s, out);
  }

  @Test
  public void create_withId_throwsBadRequest() {
    Store s = new Store();
    s.id = 123L; // invalid according to API

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.create(s));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  public void create_registersPostCommit_and_afterCommitInvokesLegacy() {
    // use a real Store instance (fields are simple public members) so we don't
    // try to stub field access on a Mockito mock (which caused MissingMethodInvocation)
    // use anon subclass to avoid calling Panache persist() which requires Arc container
    Store s = new Store() {
      @Override
      public void persist() {
        // no-op for unit test
      }
    };
    s.id = null;

    // capture the synchronization registered
    ArgumentCaptor<Synchronization> captor = ArgumentCaptor.forClass(Synchronization.class);

    resource.create(s);

    // capture the registered synchronization and simulate after-commit
    verify(txRegistry).registerInterposedSynchronization(captor.capture());
    Synchronization sync = captor.getValue();
    // simulate after commit
    sync.afterCompletion(Status.STATUS_COMMITTED);

    verify(legacy).createStoreOnLegacySystem(s);
  }

  @Test
  public void update_nullName_throwsBadRequest() {
    Store updated = new Store();
    updated.name = null; // invalid

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.update(1L, updated));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  public void patch_nullBody_throwsBadRequest() {
    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> resource.patch(1L, null));
    assertEquals(400, ex.getResponse().getStatus());
  }
}
