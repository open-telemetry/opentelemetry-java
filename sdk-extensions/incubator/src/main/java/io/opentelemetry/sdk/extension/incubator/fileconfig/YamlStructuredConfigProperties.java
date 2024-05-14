/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.annotation.Nullable;

/**
 * Implementation of {@link StructuredConfigProperties} which uses a file configuration model as a
 * source.
 *
 * @see #getStructured(String) Accessing nested maps
 * @see #getStructuredList(String) Accessing lists of maps
 * @see FileConfiguration#toConfigProperties(Object) Converting configuration model to properties
 */
final class YamlStructuredConfigProperties implements StructuredConfigProperties {

  /** Values are {@link #isPrimitive(Object)}, {@link List} of scalars. */
  private final Map<String, Object> simpleEntries = new HashMap<>();

  private final Map<String, List<StructuredConfigProperties>> listEntries = new HashMap<>();
  private final Map<String, StructuredConfigProperties> mapEntries = new HashMap<>();

  @SuppressWarnings("unchecked")
  YamlStructuredConfigProperties(Map<String, Object> properties) {
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
        List<StructuredConfigProperties> list =
            ((List<Map<String, Object>>) value)
                .stream().map(YamlStructuredConfigProperties::new).collect(toList());
        listEntries.put(key, list);
        continue;
      }
      if (isMap(value)) {
        YamlStructuredConfigProperties configProperties =
            new YamlStructuredConfigProperties((Map<String, Object>) value);
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
      return list.stream().allMatch(YamlStructuredConfigProperties::isPrimitive);
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
    if (value instanceof Long) {
      return ((Long) value).intValue();
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
  @SuppressWarnings("unchecked")
  public List<String> getScalarList(String name) {
    Object value = simpleEntries.get(name);
    if (value instanceof List) {
      return (List<String>) value;
    }
    return null;
  }

  @Nullable
  @Override
  public StructuredConfigProperties getStructured(String name) {
    return getConfigPropertiesInternal(name);
  }

  @Nullable
  @Override
  public List<StructuredConfigProperties> getStructuredList(String name) {
    return listEntries.get(name);
  }

  @Nullable
  private StructuredConfigProperties getConfigPropertiesInternal(String name) {
    return mapEntries.get(name);
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "YamlStructuredConfigProperties{", "}");
    simpleEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    listEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    mapEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    return joiner.toString();
  }
}
