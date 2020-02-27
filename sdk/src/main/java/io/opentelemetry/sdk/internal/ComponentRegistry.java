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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;

/**
 * Base class for all the provider classes (TracerProvider, MeterProvider, etc.).
 *
 * @param <V> the type of the registered value.
 */
public abstract class ComponentRegistry<V> {

  private final ConcurrentMap<InstrumentationLibraryInfo, V> registry = new ConcurrentHashMap<>();

  /**
   * Returns the registered value associated with this name and {@code null} version if any,
   * otherwise creates a new instance and associates it with the given name and {@code null}
   * version.
   *
   * @param instrumentationName the name of the instrumentation library.
   * @return the registered value associated with this name and {@code null} version.
   */
  public final V get(String instrumentationName) {
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
  public final V get(String instrumentationName, @Nullable String instrumentationVersion) {
    InstrumentationLibraryInfo instrumentationLibraryInfo =
        InstrumentationLibraryInfo.create(instrumentationName, instrumentationVersion);

    V component = registry.get(instrumentationLibraryInfo);
    if (component != null) {
      return component;
    }

    V newComponent = newComponent(instrumentationLibraryInfo);
    V oldComponent = registry.putIfAbsent(instrumentationLibraryInfo, newComponent);
    return oldComponent != null ? oldComponent : newComponent;
  }

  /**
   * Returns a {@code Collection} view of the registered components.
   *
   * @return a {@code Collection} view of the registered components.
   */
  public final Collection<V> getComponents() {
    return Collections.unmodifiableCollection(new ArrayList<>(registry.values()));
  }

  public abstract V newComponent(InstrumentationLibraryInfo instrumentationLibraryInfo);
}
