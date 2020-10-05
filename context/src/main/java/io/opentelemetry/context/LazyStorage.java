/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

// Lazy-loaded storage. Delaying storage initialization until after class initialization makes it
// much easier to avoid circular loading since there can still be references to Context as long as
// they don't depend on storage, like key() and currentContextExecutor(). It also makes it easier
// to handle exceptions.
final class LazyStorage {

  private static final String CONTEXT_STORAGE_PROVIDER_PROPERTY =
      "io.opentelemetry.context.contextStorageProvider";

  private static final Logger logger = Logger.getLogger(ThreadLocalContextStorage.class.getName());

  static final ContextStorage storage;

  static {
    AtomicReference<Throwable> deferredStorageFailure = new AtomicReference<>();
    storage = createStorage(deferredStorageFailure);
    Throwable failure = deferredStorageFailure.get();
    // Logging must happen after storage has been set, as loggers may use Context.
    if (failure != null) {
      logger.log(
          Level.WARNING, "ContextStorageProvider initialized failed. Using default", failure);
    }
  }

  private static ContextStorage createStorage(AtomicReference<Throwable> deferredStorageFailure) {
    String providerClassName = System.getProperty(CONTEXT_STORAGE_PROVIDER_PROPERTY, "");

    List<ContextStorageProvider> providers = new ArrayList<>();
    for (ContextStorageProvider provider : ServiceLoader.load(ContextStorageProvider.class)) {
      providers.add(provider);
    }

    if (providers.isEmpty()) {
      return DefaultContext.threadLocalStorage();
    }

    if (providers.size() == 1) {
      ContextStorageProvider provider = providers.get(0);
      try {
        return provider.get();
      } catch (Throwable t) {
        deferredStorageFailure.set(t);
        return DefaultContext.threadLocalStorage();
      }
    }

    if (providerClassName.isEmpty()) {
      deferredStorageFailure.set(
          new IllegalStateException(
              "Found multiple ContextStorageProvider. Set the "
                  + "io.opentelemetry.context.ContextStorageProvider property to the fully "
                  + "qualified class name of the provider to use. Falling back to default "
                  + "ContextStorage. Found providers: "
                  + providers));
      return DefaultContext.threadLocalStorage();
    }

    for (ContextStorageProvider provider : providers) {
      if (provider.getClass().getName().equals(providerClassName)) {
        return provider.get();
      }
    }

    deferredStorageFailure.set(
        new IllegalStateException(
            "io.opentelemetry.context.ContextStorageProvider property set but no matching class "
                + "could be found, requested: "
                + providerClassName
                + " but found providers: "
                + providers));
    return DefaultContext.threadLocalStorage();
  }

  private LazyStorage() {}
}
