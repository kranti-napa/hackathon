package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.transaction.Status;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class StoreResourceAnonymousInnerTest {

  static class SpyLegacy extends LegacyStoreManagerGateway {
    boolean createCalled = false;
    boolean updateCalled = false;

    @Override
    public void createStoreOnLegacySystem(Store store) {
      createCalled = true;
    }

    @Override
    public void updateStoreOnLegacySystem(Store store) {
      updateCalled = true;
    }
  }

  @Test
  public void anonymousSync_create_invokesLegacyOnCommit_only() throws Exception {
    StoreResource outer = new StoreResource();
    SpyLegacy spy = new SpyLegacy();
    // set package-private field
    outer.legacyStoreManagerGateway = spy;

    Store s = new Store("anonymousCreate");

    Class<?> anon = Class.forName("com.fulfilment.application.monolith.stores.StoreResource$1");
    Constructor<?> ctor = anon.getDeclaredConstructor(StoreResource.class, Store.class);
    ctor.setAccessible(true);
    Object instance = ctor.newInstance(outer, s);

    Method before = anon.getMethod("beforeCompletion");
    Method after = anon.getMethod("afterCompletion", int.class);

    // calling beforeCompletion should be no-op
    before.invoke(instance);

    // not committed -> should not call
    after.invoke(instance, Status.STATUS_ROLLEDBACK);
    assertFalse(spy.createCalled);

    // committed -> should call
    after.invoke(instance, Status.STATUS_COMMITTED);
    assertTrue(spy.createCalled);
  }

  @Test
  public void anonymousSync_update_invokesLegacyOnCommit_only() throws Exception {
    StoreResource outer = new StoreResource();
    SpyLegacy spy = new SpyLegacy();
    outer.legacyStoreManagerGateway = spy;

    Store s = new Store("anonymousUpdate");

    Class<?> anon = Class.forName("com.fulfilment.application.monolith.stores.StoreResource$2");
    Constructor<?> ctor = anon.getDeclaredConstructor(StoreResource.class, Store.class);
    ctor.setAccessible(true);
    Object instance = ctor.newInstance(outer, s);

    Method after = anon.getMethod("afterCompletion", int.class);

    after.invoke(instance, Status.STATUS_ROLLEDBACK);
    assertFalse(spy.updateCalled);

    after.invoke(instance, Status.STATUS_COMMITTED);
    assertTrue(spy.updateCalled);
  }

  @Test
  public void anonymousSync_patch_invokesLegacyOnCommit_only() throws Exception {
    StoreResource outer = new StoreResource();
    SpyLegacy spy = new SpyLegacy();
    outer.legacyStoreManagerGateway = spy;

    Store s = new Store("anonymousPatch");

    Class<?> anon = Class.forName("com.fulfilment.application.monolith.stores.StoreResource$3");
    Constructor<?> ctor = anon.getDeclaredConstructor(StoreResource.class, Store.class);
    ctor.setAccessible(true);
    Object instance = ctor.newInstance(outer, s);

    Method after = anon.getMethod("afterCompletion", int.class);

    after.invoke(instance, Status.STATUS_ROLLEDBACK);
    assertFalse(spy.updateCalled);

    after.invoke(instance, Status.STATUS_COMMITTED);
    assertTrue(spy.updateCalled);
  }
}
