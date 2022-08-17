/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * Component (tracer, meter, etc) registry class for all the provider classes (TracerProvider,
 * MeterProvider, etc.).
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @param <V> the type of the registered value.
 */
public final class ComponentRegistry<V> {

  private final ConcurrentMap<InstrumentationScopeInfo, V> registry = new ConcurrentHashMap<>();
  private final Function<InstrumentationScopeInfo, V> factory;

  public ComponentRegistry(Function<InstrumentationScopeInfo, V> factory) {
    this.factory = factory;
  }

  /**
   * Returns the registered value associated with this name and version if any, otherwise creates a
   * new instance and associates it with the given name and version.
   *
   * @param instrumentationScopeName the name of the instrumentation scope.
   * @param instrumentationScopeVersion the version of the instrumentation scope.
   * @param schemaUrl the URL of the OpenTelemetry schema used by the instrumentation scope.
   * @param attributes the attributes of the instrumentation scope.
   * @return the registered value associated with this name and version.
   * @since 1.4.0
   */
  public V get(
      String instrumentationScopeName,
      @Nullable String instrumentationScopeVersion,
      @Nullable String schemaUrl,
      Attributes attributes) {
    InstrumentationScopeInfo instrumentationScopeInfo =
        InstrumentationScopeInfo.create(
            instrumentationScopeName, instrumentationScopeVersion, schemaUrl, attributes);

    // Optimistic lookup, before creating the new component.
    V component = registry.get(instrumentationScopeInfo);
    if (component != null) {
      return component;
    }

    V newComponent = factory.apply(instrumentationScopeInfo);
    V oldComponent = registry.putIfAbsent(instrumentationScopeInfo, newComponent);
    return oldComponent != null ? oldComponent : newComponent;
  }

  /**
   * Returns a {@code Collection} view of the registered components.
   *
   * @return a {@code Collection} view of the registered components.
   */
  public Collection<V> getComponents() {
    return Collections.unmodifiableCollection(new ArrayList<>(registry.values()));
  }
}
