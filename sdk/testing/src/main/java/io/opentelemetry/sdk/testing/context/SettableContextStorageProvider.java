/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.context;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.ContextStorageProvider;
import io.opentelemetry.context.Scope;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/** A {@link ContextStorageProvider} which can have it's {@link ContextStorage} set at any time. */
public class SettableContextStorageProvider implements ContextStorageProvider {
  @Override
  public ContextStorage get() {
    return SettableContextStorage.INSTANCE;
  }

  /** Sets the {@link ContextStorage} to use for future context operations. */
  public static void setContextStorage(ContextStorage storage) {
    SettableContextStorage.delegate = storage;
  }

  /** Returns the current {@link ContextStorage}. */
  public static ContextStorage getContextStorage() {
    return SettableContextStorage.delegate;
  }

  private enum SettableContextStorage implements ContextStorage {
    INSTANCE;

    private static volatile ContextStorage delegate = createStorage();

    @Override
    public Scope attach(Context toAttach) {
      return delegate.attach(toAttach);
    }

    @Override
    public Context current() {
      return delegate.current();
    }

    // We reimplement provider lookup, ignoring the settable provider. It's clunky but allows
    // reconfiguring only in test, not in production.
    private static ContextStorage createStorage() {
      List<ContextStorageProvider> providers = new ArrayList<>();
      for (ContextStorageProvider provider : ServiceLoader.load(ContextStorageProvider.class)) {
        if (provider.getClass().equals(SettableContextStorageProvider.class)) {
          continue;
        }
        providers.add(provider);
      }

      if (providers.isEmpty()) {
        return ContextStorage.defaultStorage();
      }

      String providerClassName =
          System.getProperty("io.opentelemetry.context.contextStorageProvider", "");
      if (providerClassName.isEmpty()) {
        if (providers.size() == 1) {
          return providers.get(0).get();
        }
        return ContextStorage.defaultStorage();
      }

      for (ContextStorageProvider provider : providers) {
        if (provider.getClass().getName().equals(providerClassName)) {
          return provider.get();
        }
      }
      return ContextStorage.defaultStorage();
    }
  }
}
