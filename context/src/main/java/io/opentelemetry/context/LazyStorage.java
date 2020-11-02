/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright 2020 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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

  // Used by auto-instrumentation agent. Check with auto-instrumentation before making changes to
  // this method.
  static ContextStorage get() {
    return storage;
  }

  private static final String CONTEXT_STORAGE_PROVIDER_PROPERTY =
      "io.opentelemetry.context.contextStorageProvider";

  private static final Logger logger = Logger.getLogger(LazyStorage.class.getName());

  private static final ContextStorage storage;

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

  static ContextStorage createStorage(AtomicReference<Throwable> deferredStorageFailure) {
    String providerClassName = System.getProperty(CONTEXT_STORAGE_PROVIDER_PROPERTY, "");

    List<ContextStorageProvider> providers = new ArrayList<>();
    for (ContextStorageProvider provider : ServiceLoader.load(ContextStorageProvider.class)) {
      providers.add(provider);
    }

    if (providers.isEmpty()) {
      return ThreadLocalContextStorage.INSTANCE;
    }

    if (providerClassName.isEmpty()) {
      if (providers.size() == 1) {
        return providers.get(0).get();
      }

      deferredStorageFailure.set(
          new IllegalStateException(
              "Found multiple ContextStorageProvider. Set the "
                  + "io.opentelemetry.context.ContextStorageProvider property to the fully "
                  + "qualified class name of the provider to use. Falling back to default "
                  + "ContextStorage. Found providers: "
                  + providers));
      return ThreadLocalContextStorage.INSTANCE;
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
    return ThreadLocalContextStorage.INSTANCE;
  }

  private LazyStorage() {}
}
