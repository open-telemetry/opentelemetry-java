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
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * Base class for all the provider classes (TracerProvider, MeterProvider, etc.).
 *
 * @param <V> the type of the registered value.
 */
public final class ComponentRegistry<V> {

  private final ConcurrentMap<InstrumentationLibraryInfo, V> registry = new ConcurrentHashMap<>();
  private final Function<InstrumentationLibraryInfo, V> factory;

  public ComponentRegistry(Function<InstrumentationLibraryInfo, V> factory) {
    this.factory = factory;
  }

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
    return get(instrumentationName, instrumentationVersion, null);
  }

  /**
   * Returns the registered value associated with this name and version if any, otherwise creates a
   * new instance and associates it with the given name and version.
   *
   * @param instrumentationName the name of the instrumentation library.
   * @param instrumentationVersion the version of the instrumentation library.
   * @param schemaUrl the URL of the OpenTelemetry schema used by the instrumentation library.
   * @return the registered value associated with this name and version.
   */
  public final V get(
      String instrumentationName,
      @Nullable String instrumentationVersion,
      @Nullable String schemaUrl) {
    InstrumentationLibraryInfo instrumentationLibraryInfo =
        InstrumentationLibraryInfo.create(instrumentationName, instrumentationVersion, schemaUrl);

    // Optimistic lookup, before creating the new component.
    V component = registry.get(instrumentationLibraryInfo);
    if (component != null) {
      return component;
    }

    V newComponent = factory.apply(instrumentationLibraryInfo);
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
}
