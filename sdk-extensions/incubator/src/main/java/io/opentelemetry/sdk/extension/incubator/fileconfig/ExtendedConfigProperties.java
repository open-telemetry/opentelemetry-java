/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.annotation.Nullable;

/**
 * An extension of {@link ConfigProperties} with methods for accessing nested complex types.
 *
 * <p>See {@link #getConfigProperties(String)} for accessing nested maps.
 *
 * <p>See {@link #getListConfigProperties(String)} (String)} for accessing lists of maps.
 */
public final class ExtendedConfigProperties implements ConfigProperties {

  /**
   * Values are {@link #isPrimitive(Object)}, {@code List<String>}, or {@code Map<String, String>}.
   */
  private final Map<String, Object> simpleEntries = new HashMap<>();

  private final Map<String, List<ExtendedConfigProperties>> listEntries = new HashMap<>();
  private final Map<String, ExtendedConfigProperties> mapEntries = new HashMap<>();

  @SuppressWarnings("unchecked")
  ExtendedConfigProperties(Map<String, Object> properties) {
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (isPrimitive(value)) {
        simpleEntries.put(key, value);
        continue;
      }
      if (isPrimitiveList(value)) {
        simpleEntries.put(key, ((List<?>) value).stream().map(String::valueOf).collect(toList()));
        continue;
      }
      if (isListOfMaps(value)) {
        List<ExtendedConfigProperties> list =
            ((List<Map<String, Object>>) value)
                .stream().map(ExtendedConfigProperties::new).collect(toList());
        listEntries.put(key, list);
        continue;
      }
      if (isMap(value)) {
        ExtendedConfigProperties configProperties =
            new ExtendedConfigProperties((Map<String, Object>) value);
        mapEntries.put(key, configProperties);
        continue;
      }
      throw new ConfigurationException(
          "Unable to initialize ExtendedConfigProperties. Key \""
              + key
              + "\" has unrecognized object type "
              + value.getClass().getName());
    }
  }

  private static boolean isPrimitiveList(Object object) {
    if (object instanceof List) {
      List<?> list = (List<?>) object;
      return list.stream().allMatch(ExtendedConfigProperties::isPrimitive);
    }
    return false;
  }

  private static boolean isPrimitive(Object object) {
    return object instanceof String
        || object instanceof Integer
        || object instanceof Long
        || object instanceof Float
        || object instanceof Double
        || object instanceof Boolean;
  }

  private static boolean isListOfMaps(Object object) {
    if (object instanceof List) {
      List<?> list = (List<?>) object;
      return list.stream()
          .allMatch(
              entry ->
                  entry instanceof Map
                      && ((Map<?, ?>) entry)
                          .keySet().stream().allMatch(key -> key instanceof String));
    }
    return false;
  }

  private static boolean isMap(Object object) {
    if (object instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) object;
      return map.keySet().stream().allMatch(entry -> entry instanceof String);
    }
    return false;
  }

  @Nullable
  @Override
  public String getString(String name) {
    Object value = simpleEntries.get(name);
    if (value instanceof String) {
      return (String) value;
    }
    return null;
  }

  @Nullable
  @Override
  public Boolean getBoolean(String name) {
    Object value = simpleEntries.get(name);
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return null;
  }

  @Nullable
  @Override
  public Integer getInt(String name) {
    Object value = simpleEntries.get(name);
    if (value instanceof Integer) {
      return (Integer) value;
    }
    return null;
  }

  @Nullable
  @Override
  public Long getLong(String name) {
    Object value = simpleEntries.get(name);
    if (value instanceof Integer) {
      return ((Integer) value).longValue();
    }
    if (value instanceof Long) {
      return (Long) value;
    }
    return null;
  }

  @Nullable
  @Override
  public Double getDouble(String name) {
    Object value = simpleEntries.get(name);
    if (value instanceof Float) {
      return ((Float) value).doubleValue();
    }
    if (value instanceof Double) {
      return (Double) value;
    }
    return null;
  }

  @Nullable
  @Override
  public Duration getDuration(String name) {
    // TODO(jack-berg): implement
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> getList(String name) {
    Object value = simpleEntries.get(name);
    if (value instanceof List) {
      return (List<String>) value;
    }
    return Collections.emptyList();
  }

  @Override
  public Map<String, String> getMap(String name) {
    throw new UnsupportedOperationException("Use getConfigProperties(String) instead");
  }

  @Nullable
  public ExtendedConfigProperties getConfigProperties(String name) {
    return mapEntries.get(name);
  }

  @Nullable
  public List<ExtendedConfigProperties> getListConfigProperties(String name) {
    return listEntries.get(name);
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "ExtendedConfigProperties{", "}");
    simpleEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    listEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    mapEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    return joiner.toString();
  }
}
