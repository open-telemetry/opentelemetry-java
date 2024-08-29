/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
  private final Map<String, Object> simpleEntries;

  private final Map<String, List<YamlStructuredConfigProperties>> listEntries;
  private final Map<String, YamlStructuredConfigProperties> mapEntries;
  private final Set<String> nullKeys;

  private YamlStructuredConfigProperties(
      Map<String, Object> simpleEntries,
      Map<String, List<YamlStructuredConfigProperties>> listEntries,
      Map<String, YamlStructuredConfigProperties> mapEntries,
      Set<String> nullKeys) {
    this.simpleEntries = simpleEntries;
    this.listEntries = listEntries;
    this.mapEntries = mapEntries;
    this.nullKeys = nullKeys;
  }

  /**
   * Create a {@link YamlStructuredConfigProperties} from the {@code properties} map.
   *
   * <p>{@code properties} is expected to be the output of YAML parsing (i.e. with Jackson {@link
   * com.fasterxml.jackson.databind.ObjectMapper}), and have values which are scalars, lists of
   * scalars, lists of maps, and maps.
   *
   * @see FileConfiguration#toConfigProperties(OpenTelemetryConfiguration)
   */
  @SuppressWarnings("unchecked")
  static YamlStructuredConfigProperties create(Map<String, Object> properties) {
    Map<String, Object> simpleEntries = new HashMap<>();
    Map<String, List<YamlStructuredConfigProperties>> listEntries = new HashMap<>();
    Map<String, YamlStructuredConfigProperties> mapEntries = new HashMap<>();
    Set<String> nullEntries = new HashSet<>();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value == null) {
        nullEntries.add(key);
        continue;
      }
      if (isPrimitive(value)) {
        simpleEntries.put(key, value);
        continue;
      }
      if (isPrimitiveList(value)) {
        simpleEntries.put(key, value);
        continue;
      }
      if (isListOfMaps(value)) {
        List<YamlStructuredConfigProperties> list =
            ((List<Map<String, Object>>) value)
                .stream().map(YamlStructuredConfigProperties::create).collect(toList());
        listEntries.put(key, list);
        continue;
      }
      if (isMap(value)) {
        YamlStructuredConfigProperties configProperties =
            YamlStructuredConfigProperties.create((Map<String, Object>) value);
        mapEntries.put(key, configProperties);
        continue;
      }
      throw new ConfigurationException(
          "Unable to initialize ExtendedConfigProperties. Key \""
              + key
              + "\" has unrecognized object type "
              + value.getClass().getName());
    }
    return new YamlStructuredConfigProperties(simpleEntries, listEntries, mapEntries, nullEntries);
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
    return stringOrNull(simpleEntries.get(name));
  }

  @Nullable
  @Override
  public Boolean getBoolean(String name) {
    return booleanOrNull(simpleEntries.get(name));
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
    return longOrNull(simpleEntries.get(name));
  }

  @Nullable
  @Override
  public Double getDouble(String name) {
    return doubleOrNull(simpleEntries.get(name));
  }

  private static final Set<Class<?>> SUPPORTED_SCALAR_TYPES =
      Collections.unmodifiableSet(
          new HashSet<>(Arrays.asList(String.class, Boolean.class, Long.class, Double.class)));

  @Nullable
  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> getScalarList(String name, Class<T> scalarType) {
    if (!SUPPORTED_SCALAR_TYPES.contains(scalarType)) {
      throw new ConfigurationException(
          "Unsupported scalar type "
              + scalarType.getName()
              + ". Supported types include "
              + SUPPORTED_SCALAR_TYPES.stream()
                  .map(Class::getName)
                  .collect(joining(",", "[", "]")));
    }
    Object value = simpleEntries.get(name);
    if (value instanceof List) {
      return (List<T>)
          ((List<Object>) value)
              .stream()
                  .map(
                      entry -> {
                        if (scalarType == String.class) {
                          return stringOrNull(entry);
                        } else if (scalarType == Boolean.class) {
                          return booleanOrNull(entry);
                        } else if (scalarType == Long.class) {
                          return longOrNull(entry);
                        } else if (scalarType == Double.class) {
                          return doubleOrNull(entry);
                        }
                        return null;
                      })
                  .filter(Objects::nonNull)
                  .collect(toList());
    }
    return null;
  }

  @Nullable
  private static String stringOrNull(@Nullable Object value) {
    if (value instanceof String) {
      return (String) value;
    }
    return null;
  }

  @Nullable
  private static Boolean booleanOrNull(@Nullable Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return null;
  }

  @Nullable
  private static Long longOrNull(@Nullable Object value) {
    if (value instanceof Integer) {
      return ((Integer) value).longValue();
    }
    if (value instanceof Long) {
      return (Long) value;
    }
    return null;
  }

  @Nullable
  private static Double doubleOrNull(@Nullable Object value) {
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
  public StructuredConfigProperties getStructured(String name) {
    return mapEntries.get(name);
  }

  @Nullable
  @Override
  public List<StructuredConfigProperties> getStructuredList(String name) {
    List<YamlStructuredConfigProperties> value = listEntries.get(name);
    if (value != null) {
      return Collections.unmodifiableList(value);
    }
    return null;
  }

  @Override
  public Set<String> getPropertyKeys() {
    Set<String> keys = new HashSet<>();
    keys.addAll(nullKeys);
    keys.addAll(simpleEntries.keySet());
    keys.addAll(listEntries.keySet());
    keys.addAll(mapEntries.keySet());
    return Collections.unmodifiableSet(keys);
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "YamlStructuredConfigProperties{", "}");
    nullKeys.forEach((key) -> joiner.add(key + "=null"));
    simpleEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    listEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    mapEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    return joiner.toString();
  }

  /** Return a map representation of the data. */
  public Map<String, Object> toMap() {
    Map<String, Object> result = new HashMap<>();
    nullKeys.forEach(key -> result.put(key, null));
    result.putAll(simpleEntries);
    listEntries.forEach(
        (key, value) ->
            result.put(
                key, value.stream().map(YamlStructuredConfigProperties::toMap).collect(toList())));
    mapEntries.forEach((key, value) -> result.put(key, value.toMap()));
    return Collections.unmodifiableMap(result);
  }
}
