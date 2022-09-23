/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import com.google.auto.value.AutoValue;
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

  private final ConcurrentMap<ScopeKey, V> registry = new ConcurrentHashMap<>();
  private final Function<InstrumentationScopeInfo, V> factory;

  public ComponentRegistry(Function<InstrumentationScopeInfo, V> factory) {
    this.factory = factory;
  }

  /**
   * Returns the registered value associated with this {@link InstrumentationScopeInfo scope} if
   * any, otherwise creates a new instance and associates it with the given scope.
   */
  public V get(InstrumentationScopeInfo instrumentationScopeInfo) {
    ScopeKey scopeKey = toScopeKey(instrumentationScopeInfo);

    // Optimistic lookup, before creating the new component.
    V component = registry.get(scopeKey);
    if (component != null) {
      return component;
    }

    V newComponent = factory.apply(instrumentationScopeInfo);
    V oldComponent = registry.putIfAbsent(scopeKey, newComponent);
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

  private static ScopeKey toScopeKey(InstrumentationScopeInfo instrumentationScopeInfo) {
    return new AutoValue_ComponentRegistry_ScopeKey(
        instrumentationScopeInfo.getName(),
        instrumentationScopeInfo.getVersion(),
        instrumentationScopeInfo.getSchemaUrl());
  }

  /**
   * Encapsulates the {@link InstrumentationScopeInfo} fields which identify a unique component
   * (Tracer, Meter, Logger).
   */
  @AutoValue
  abstract static class ScopeKey {

    abstract String getName();

    @Nullable
    abstract String getVersion();

    @Nullable
    abstract String getSchemaUrl();
  }
}
