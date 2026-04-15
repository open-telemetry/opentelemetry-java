/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * Component (tracer, meter, etc) registry class for all the provider classes (TracerProvider,
 * MeterProvider, etc.).
 *
 * <p>Components are identified by name, version, and schema. Name is required, but version and
 * schema are optional. Therefore, we have 4 possible scenarios for component keys:
 *
 * <ol>
 *   <li>Only name is provided, represented by {@link #componentByName}
 *   <li>Name and version are provided, represented by {@link #componentByNameAndVersion}
 *   <li>Name and schema are provided, represented by {@link #componentByNameAndSchema}
 *   <li>Name, version and schema are provided, represented by {@link
 *       #componentByNameVersionAndSchema}
 * </ol>
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @param <V> the type of the registered value.
 */
public final class ComponentRegistry<V> {

  private final Map<String, V> componentByName = new ConcurrentHashMap<>();
  private final Map<String, Map<String, V>> componentByNameAndVersion = new ConcurrentHashMap<>();
  private final Map<String, Map<String, V>> componentByNameAndSchema = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Map<String, V>>> componentByNameVersionAndSchema =
      new ConcurrentHashMap<>();

  private final Object lock = new Object();

  @GuardedBy("lock")
  private final Set<V> allComponents = Collections.newSetFromMap(new IdentityHashMap<>());

  private final Function<InstrumentationScopeInfo, V> factory;

  public ComponentRegistry(Function<InstrumentationScopeInfo, V> factory) {
    this.factory = factory;
  }

  /**
   * Returns the component associated with the {@code name}, {@code version}, and {@code schemaUrl}.
   * {@link Attributes} are not part of component identity. Behavior is undefined when different
   * {@link Attributes} are provided where {@code name}, {@code version}, and {@code schemaUrl} are
   * identical.
   */
  public V get(
      String name, @Nullable String version, @Nullable String schemaUrl, Attributes attributes) {
    if (version != null && schemaUrl != null) {
      Map<String, Map<String, V>> componentByVersionAndSchema =
          componentByNameVersionAndSchema.computeIfAbsent(
              name, unused -> new ConcurrentHashMap<>());
      Map<String, V> componentBySchema =
          componentByVersionAndSchema.computeIfAbsent(version, unused -> new ConcurrentHashMap<>());
      return componentBySchema.computeIfAbsent(
          schemaUrl,
          schemaUrl1 ->
              buildComponent(
                  InstrumentationScopeInfo.builder(name)
                      .setVersion(version)
                      .setSchemaUrl(schemaUrl1)
                      .setAttributes(attributes)
                      .build()));
    } else if (version != null) { // schemaUrl == null
      Map<String, V> componentByVersion =
          componentByNameAndVersion.computeIfAbsent(name, unused -> new ConcurrentHashMap<>());
      return componentByVersion.computeIfAbsent(
          version,
          version1 ->
              buildComponent(
                  InstrumentationScopeInfo.builder(name)
                      .setVersion(version1)
                      .setAttributes(attributes)
                      .build()));
    }
    if (schemaUrl != null) { // version == null
      Map<String, V> componentBySchema =
          componentByNameAndSchema.computeIfAbsent(name, unused -> new ConcurrentHashMap<>());
      return componentBySchema.computeIfAbsent(
          schemaUrl,
          schemaUrl1 ->
              buildComponent(
                  InstrumentationScopeInfo.builder(name)
                      .setSchemaUrl(schemaUrl1)
                      .setAttributes(attributes)
                      .build()));
    } else { // schemaUrl == null && version == null
      return componentByName.computeIfAbsent(
          name,
          name1 ->
              buildComponent(
                  InstrumentationScopeInfo.builder(name1).setAttributes(attributes).build()));
    }
  }

  private V buildComponent(InstrumentationScopeInfo instrumentationScopeInfo) {
    V component = factory.apply(instrumentationScopeInfo);
    synchronized (lock) {
      allComponents.add(component);
    }
    return component;
  }

  /**
   * Returns a {@code Collection} view of the registered components.
   *
   * @return a {@code Collection} view of the registered components.
   */
  public Collection<V> getComponents() {
    synchronized (lock) {
      return Collections.unmodifiableCollection(new ArrayList<>(allComponents));
    }
  }
}
