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

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

/**
 * Base class for all the registry classes (Tracer, Meter, etc.).
 *
 * @param <V> the type of the registered value.
 */
public abstract class ComponentRegistry<V> {
  private final Object lock = new Object();
  private final Map<InstrumentationLibraryInfo, V> registry = new ConcurrentHashMap<>();

  /**
   * Returns the registered value associated with this name and {@code null} version if any,
   * otherwise creates a new instance and associates it with the given name and {@code null}
   * version.
   *
   * @param instrumentationName the name of the instrumentation library.
   * @return the registered value associated with this name and {@code null} version.
   */
  public V get(String instrumentationName) {
    return get(instrumentationName, null);
  }

  /**
   * Returns the registered value associated with this name and version if any, otherwise creates a
   * new instance and associates it with the given name and version.
   *
   * @param instrumentationName the name of the instrumentation library.
   * @param instrumentationVersion the version of the instrumentation library.
   * @return the registered value associated with this name and version.
   */
  public V get(String instrumentationName, @Nullable String instrumentationVersion) {
    InstrumentationLibraryInfo instrumentationLibraryInfo =
        InstrumentationLibraryInfo.create(instrumentationName, instrumentationVersion);
    V tracer = registry.get(instrumentationLibraryInfo);
    if (tracer == null) {
      synchronized (lock) {
        // Re-check if the value was added since the previous check, this can happen if multiple
        // threads try to access the same named tracer during the same time. This way we ensure that
        // we create only one TracerSdk per name.
        tracer = registry.get(instrumentationLibraryInfo);
        if (tracer != null) {
          // A different thread already added the named Tracer, just reuse.
          return tracer;
        }
        tracer = newComponent(instrumentationLibraryInfo);
        registry.put(instrumentationLibraryInfo, tracer);
      }
    }
    return tracer;
  }

  public abstract V newComponent(InstrumentationLibraryInfo instrumentationLibraryInfo);
}
