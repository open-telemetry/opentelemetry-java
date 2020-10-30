/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
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
