/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import io.opentelemetry.common.ComponentLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * A {@link DeclarativeConfigProperties} implementation backed by a {@code Map<String, Object>}.
 *
 * <p>This is the inverse of {@link DeclarativeConfigProperties#toMap(DeclarativeConfigProperties)}.
 * Values in the map are expected to follow the same conventions as YAML parsing output: scalars
 * (String, Boolean, Long, Double, Integer), lists of scalars, maps (structured children), and lists
 * of maps (structured lists).
 */
final class MapBackedDeclarativeConfigProperties implements DeclarativeConfigProperties {

  private final Map<String, Object> values;
  private final ComponentLoader componentLoader;

  MapBackedDeclarativeConfigProperties(
      Map<String, Object> values, ComponentLoader componentLoader) {
    this.values = values;
    this.componentLoader = componentLoader;
  }

  @Nullable
  @Override
  public String getString(String name) {
    Object value = values.get(name);
    return value instanceof String ? (String) value : null;
  }

  @Nullable
  @Override
  public Boolean getBoolean(String name) {
    Object value = values.get(name);
    return value instanceof Boolean ? (Boolean) value : null;
  }

  @Nullable
  @Override
  public Integer getInt(String name) {
    Object value = values.get(name);
    if (value instanceof Integer) {
      return (Integer) value;
    }
    if (value instanceof Long) {
      return ((Long) value).intValue();
    }
    return null;
  }

  @Nullable
  @Override
  public Long getLong(String name) {
    Object value = values.get(name);
    if (value instanceof Long) {
      return (Long) value;
    }
    if (value instanceof Integer) {
      return ((Integer) value).longValue();
    }
    return null;
  }

  @Nullable
  @Override
  public Double getDouble(String name) {
    Object value = values.get(name);
    if (value instanceof Double) {
      return (Double) value;
    }
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T> List<T> getScalarList(String name, Class<T> scalarType) {
    Object value = values.get(name);
    if (!(value instanceof List)) {
      return null;
    }
    List<Object> raw = (List<Object>) value;
    List<T> casted = new ArrayList<>(raw.size());
    for (Object element : raw) {
      if (!scalarType.isInstance(element)) {
        return null;
      }
      casted.add(scalarType.cast(element));
    }
    return casted;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public DeclarativeConfigProperties getStructured(String name) {
    Object value = values.get(name);
    if (!(value instanceof Map)) {
      return null;
    }
    return new MapBackedDeclarativeConfigProperties((Map<String, Object>) value, componentLoader);
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public List<DeclarativeConfigProperties> getStructuredList(String name) {
    Object value = values.get(name);
    if (!(value instanceof List)) {
      return null;
    }
    List<Object> raw = (List<Object>) value;
    List<DeclarativeConfigProperties> result = new ArrayList<>(raw.size());
    for (Object element : raw) {
      if (!(element instanceof Map)) {
        return null;
      }
      result.add(
          new MapBackedDeclarativeConfigProperties((Map<String, Object>) element, componentLoader));
    }
    return result;
  }

  @Override
  public Set<String> getPropertyKeys() {
    return values.keySet();
  }

  @Override
  public ComponentLoader getComponentLoader() {
    return componentLoader;
  }
}
