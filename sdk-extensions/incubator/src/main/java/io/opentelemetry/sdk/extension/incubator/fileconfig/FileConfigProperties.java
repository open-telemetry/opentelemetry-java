/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ExtendedConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import javax.annotation.Nullable;

/**
 * Implementation of {@link ExtendedConfigProperties} which uses the file {@link
 * OpenTelemetryConfiguration} model as a source.
 *
 * @see #getConfigProperties(String) Accessing nested maps
 * @see #getListConfigProperties(String) Accessing lists of maps
 * @see ConfigurationFactory#toConfigProperties(OpenTelemetryConfiguration) Converting configuration
 *     model to properties
 */
final class FileConfigProperties implements ExtendedConfigProperties {

  /**
   * Values are {@link #isPrimitive(Object)}, {@code List<String>}, or {@code Map<String, String>}.
   */
  private final Map<String, Object> simpleEntries = new HashMap<>();

  private final Map<String, List<ExtendedConfigProperties>> listEntries = new HashMap<>();
  private final Map<String, ExtendedConfigProperties> mapEntries = new HashMap<>();

  @SuppressWarnings("unchecked")
  FileConfigProperties(Map<String, Object> properties) {
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
                .stream().map(FileConfigProperties::new).collect(toList());
        listEntries.put(key, list);
        continue;
      }
      if (isMap(value)) {
        FileConfigProperties configProperties =
            new FileConfigProperties((Map<String, Object>) value);
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
      return list.stream().allMatch(FileConfigProperties::isPrimitive);
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
    if (value == null && hasDotNotation(name)) {
      return applyToChild(name, ExtendedConfigProperties::getString);
    }
    if (value instanceof String) {
      return (String) value;
    }
    return null;
  }

  @Nullable
  @Override
  public Boolean getBoolean(String name) {
    Object value = simpleEntries.get(name);
    if (value == null && hasDotNotation(name)) {
      return applyToChild(name, ExtendedConfigProperties::getBoolean);
    }
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return null;
  }

  @Nullable
  @Override
  public Integer getInt(String name) {
    Object value = simpleEntries.get(name);
    if (value == null && hasDotNotation(name)) {
      return applyToChild(name, ExtendedConfigProperties::getInt);
    }
    if (value instanceof Integer) {
      return (Integer) value;
    }
    return null;
  }

  @Nullable
  @Override
  public Long getLong(String name) {
    Object value = simpleEntries.get(name);
    if (value == null && hasDotNotation(name)) {
      return applyToChild(name, ExtendedConfigProperties::getLong);
    }
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
    if (value == null && hasDotNotation(name)) {
      return applyToChild(name, ExtendedConfigProperties::getDouble);
    }
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
    // Note: unlike DefaultConfigProperties, all FileConfigProperties durations are integers / longs
    // interpreted as millis. The unit suffixes (ms, s, m, h, d) are not supported.
    Long millis = getLong(name);
    if (millis == null) {
      return null;
    }
    return Duration.ofMillis(millis);
  }

  @Override
  @SuppressWarnings({"unchecked", "NullAway"})
  public List<String> getList(String name) {
    Object value = simpleEntries.get(name);
    if (value == null && hasDotNotation(name)) {
      List<String> result = applyToChild(name, ExtendedConfigProperties::getList);
      if (result != null) {
        return result;
      }
      return Collections.emptyList();
    }
    if (value instanceof List) {
      return (List<String>) value;
    }
    return Collections.emptyList();
  }

  @Override
  public Map<String, String> getMap(String name) {
    // NOTE: we can return more generic ExtendedConfigProperties map entries, but not a simpler
    // Map<String, String> version. So we return an empty map rather than throwing or trying to
    // downgrade ExtendedConfigProperties to Map<String, String>
    return Collections.emptyMap();
  }

  @Nullable
  @Override
  public ExtendedConfigProperties getConfigProperties(String name) {
    ExtendedConfigProperties value = getConfigPropertiesInternal(name);
    if (value == null && hasDotNotation(name)) {
      return applyToChild(name, ExtendedConfigProperties::getConfigProperties);
    }
    return value;
  }

  @Nullable
  @Override
  public List<ExtendedConfigProperties> getListConfigProperties(String name) {
    List<ExtendedConfigProperties> value = listEntries.get(name);
    if (value == null && hasDotNotation(name)) {
      return applyToChild(name, ExtendedConfigProperties::getListConfigProperties);
    }
    return value;
  }

  @Nullable
  private ExtendedConfigProperties getConfigPropertiesInternal(String name) {
    return mapEntries.get(name);
  }

  private static boolean hasDotNotation(String name) {
    return name.contains(".");
  }

  /** Should only be called after {@link #hasDotNotation(String)} returns {@code true}. */
  @Nullable
  private <T> T applyToChild(
      String name, BiFunction<ExtendedConfigProperties, String, T> function) {
    String[] parts = name.split("\\.", 2);
    if (parts.length != 2) {
      throw new IllegalStateException(
          "applyToChild called without hasDotNotation. This is a programming error.");
    }
    ExtendedConfigProperties childProps = getConfigPropertiesInternal(parts[0]);
    if (childProps == null) {
      return null;
    }
    return function.apply(childProps, parts[1]);
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "FileConfigProperties{", "}");
    simpleEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    listEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    mapEntries.forEach((key, value) -> joiner.add(key + "=" + value));
    return joiner.toString();
  }
}
