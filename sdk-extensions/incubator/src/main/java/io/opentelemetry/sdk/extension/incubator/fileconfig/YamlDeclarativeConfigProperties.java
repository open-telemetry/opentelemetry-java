/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.annotation.Nullable;

/**
 * Implementation of {@link DeclarativeConfigProperties} which uses a file configuration model as a
 * source.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @see #getStructured(String) Accessing nested maps
 * @see #getStructuredList(String) Accessing lists of maps
 * @see DeclarativeConfiguration#toConfigProperties(Object, ComponentLoader) Converting
 *     configuration model to properties
 */
public final class YamlDeclarativeConfigProperties implements DeclarativeConfigProperties {

  private static final Set<Class<?>> SUPPORTED_SCALAR_TYPES =
      Collections.unmodifiableSet(
          new LinkedHashSet<>(
              Arrays.asList(String.class, Boolean.class, Long.class, Double.class)));

  /** Values are {@link #isPrimitive(Object)}, {@link List} of scalars. */
  private final Map<String, Object> simpleEntries;

  private final Map<String, List<YamlDeclarativeConfigProperties>> listEntries;
  private final Map<String, YamlDeclarativeConfigProperties> mapEntries;
  private final ComponentLoader componentLoader;

  private YamlDeclarativeConfigProperties(
      Map<String, Object> simpleEntries,
      Map<String, List<YamlDeclarativeConfigProperties>> listEntries,
      Map<String, YamlDeclarativeConfigProperties> mapEntries,
      ComponentLoader componentLoader) {
    this.simpleEntries = simpleEntries;
    this.listEntries = listEntries;
    this.mapEntries = mapEntries;
    this.componentLoader = componentLoader;
  }

  /**
   * Create a {@link YamlDeclarativeConfigProperties} from the {@code properties} map.
   *
   * <p>{@code properties} is expected to be the output of YAML parsing (i.e. with Jackson {@code
   * com.fasterxml.jackson.databind.ObjectMapper}), and have values which are scalars, lists of
   * scalars, lists of maps, and maps.
   *
   * @see DeclarativeConfiguration#toConfigProperties(Object)
   */
  @SuppressWarnings("unchecked")
  public static YamlDeclarativeConfigProperties create(
      Map<String, Object> properties, ComponentLoader componentLoader) {
    Map<String, Object> simpleEntries = new LinkedHashMap<>();
    Map<String, List<YamlDeclarativeConfigProperties>> listEntries = new LinkedHashMap<>();
    Map<String, YamlDeclarativeConfigProperties> mapEntries = new LinkedHashMap<>();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (isPrimitive(value) || value == null) {
        simpleEntries.put(key, value);
        continue;
      }
      if (isPrimitiveList(value)) {
        simpleEntries.put(key, value);
        continue;
      }
      if (isListOfMaps(value)) {
        List<YamlDeclarativeConfigProperties> list =
            ((List<Map<String, Object>>) value)
                .stream()
                    .map(map -> YamlDeclarativeConfigProperties.create(map, componentLoader))
                    .collect(toList());
        listEntries.put(key, list);
        continue;
      }
      if (isMap(value)) {
        YamlDeclarativeConfigProperties configProperties =
            YamlDeclarativeConfigProperties.create((Map<String, Object>) value, componentLoader);
        mapEntries.put(key, configProperties);
        continue;
      }
      throw new DeclarativeConfigException(
          "Unable to initialize ExtendedConfigProperties. Key \""
              + key
              + "\" has unrecognized object type "
              + value.getClass().getName());
    }
    return new YamlDeclarativeConfigProperties(
        simpleEntries, listEntries, mapEntries, componentLoader);
  }

  private static boolean isPrimitiveList(Object object) {
    if (object instanceof List) {
      List<?> list = (List<?>) object;
      return list.stream().allMatch(YamlDeclarativeConfigProperties::isPrimitive);
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

  @Nullable
  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> getScalarList(String name, Class<T> scalarType) {
    if (!SUPPORTED_SCALAR_TYPES.contains(scalarType)) {
      throw new DeclarativeConfigException(
          "Unsupported scalar type "
              + scalarType.getName()
              + ". Supported types include "
              + SUPPORTED_SCALAR_TYPES.stream()
                  .map(Class::getName)
                  .collect(joining(",", "[", "]")));
    }
    Object value = simpleEntries.get(name);
    if (value instanceof List) {
      List<Object> objectList = ((List<Object>) value);
      if (objectList.isEmpty()) {
        return Collections.emptyList();
      }
      List<T> result =
          (List<T>)
              objectList.stream()
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
      if (result.isEmpty()) {
        return null;
      }
      return result;
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
  public DeclarativeConfigProperties getStructured(String name) {
    return mapEntries.get(name);
  }

  @Nullable
  @Override
  public List<DeclarativeConfigProperties> getStructuredList(String name) {
    List<YamlDeclarativeConfigProperties> value = listEntries.get(name);
    if (value != null) {
      return Collections.unmodifiableList(value);
    }
    return null;
  }

  @Override
  public Set<String> getPropertyKeys() {
    Set<String> keys = new LinkedHashSet<>();
    keys.addAll(simpleEntries.keySet());
    keys.addAll(listEntries.keySet());
    keys.addAll(mapEntries.keySet());
    return Collections.unmodifiableSet(keys);
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "YamlDeclarativeConfigProperties{", "}");
    simpleEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    listEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    mapEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    return joiner.toString();
  }

  /** Return the {@link ComponentLoader}. */
  @Override
  public ComponentLoader getComponentLoader() {
    return componentLoader;
  }
}
