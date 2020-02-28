/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.grpc.override;

import io.grpc.Context;
import io.grpc.override.ContextStorageListener.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/** If moved to grpc only the LazyStorage needs to move. */
public final class ContextStorageOverride extends Context.Storage {
  private final Context.Storage contextStorageImpl;

  /** Public constructor to be loaded by the {@link Context}. */
  public ContextStorageOverride() {
    contextStorageImpl = LazyStorage.storage;
  }

  @Override
  public Context doAttach(Context toAttach) {
    return contextStorageImpl.doAttach(toAttach);
  }

  @Override
  public void detach(Context toDetach, Context toRestore) {
    contextStorageImpl.detach(toDetach, toRestore);
  }

  @Override
  public Context current() {
    return contextStorageImpl.current();
  }

  private static final class LazyStorage {
    static final Context.Storage storage = wrapWithListeners(new ThreadLocalContextStorage());

    private static Context.Storage wrapWithListeners(Context.Storage impl) {
      List<Provider> providers = loadSpi(ContextStorageListener.Provider.class);
      if (providers.isEmpty()) {
        // No need to use ContextStorageWithListeners
        return impl;
      }
      List<ContextStorageListener> listeners = new ArrayList<>(providers.size());
      for (ContextStorageListener.Provider provider : providers) {
        listeners.add(provider.create());
      }
      return new ContextStorageWithListeners(impl, listeners);
    }

    private static <T> List<T> loadSpi(Class<T> providerClass) {
      List<T> result = new ArrayList<>();
      String specifiedProvider = System.getProperty(providerClass.getName());
      ServiceLoader<T> providers = ServiceLoader.load(providerClass);
      for (T provider : providers) {
        if (specifiedProvider == null || specifiedProvider.equals(provider.getClass().getName())) {
          result.add(provider);
        }
      }
      if (specifiedProvider != null) {
        throw new IllegalStateException(
            String.format("Service provider %s not found", specifiedProvider));
      }
      return result;
    }
  }
}
