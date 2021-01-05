/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

class ConfigProperties {

  static ConfigProperties get() {
    return new ConfigProperties(System.getProperties(), System.getenv());
  }

  // Visible for testing
  @SuppressWarnings({"unchecked", "rawtypes"})
  static ConfigProperties createForTest(Map<String, String> properties) {
    return new ConfigProperties((Map) properties, Collections.emptyMap());
  }

  private final Map<String, String> config;

  private ConfigProperties(
      Map<Object, Object> systemProperties, Map<String, String> environmentVariables) {
    Map<String, String> config = new HashMap<>();
    environmentVariables.forEach(
        (name, value) -> config.put(name.toLowerCase(Locale.ROOT).replace('_', '.'), value));
    systemProperties.forEach(
        (key, value) -> config.put(((String) key).toLowerCase(Locale.ROOT), (String) value));

    this.config = config;
  }

  @Nullable
  String getString(String name) {
    return config.get(name);
  }

  @Nullable
  @SuppressWarnings("UnusedException")
  Integer getInt(String name) {
    String value = config.get(name);
    if (value == null || value.isEmpty()) {
      return null;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      throw newInvalidPropertyException(name, value, "integer");
    }
  }

  @Nullable
  @SuppressWarnings("UnusedException")
  Long getLong(String name) {
    String value = config.get(name);
    if (value == null || value.isEmpty()) {
      return null;
    }
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException ex) {
      throw newInvalidPropertyException(name, value, "long");
    }
  }

  @Nullable
  @SuppressWarnings("UnusedException")
  Double getDouble(String name) {
    String value = config.get(name);
    if (value == null || value.isEmpty()) {
      return null;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException ex) {
      throw newInvalidPropertyException(name, value, "double");
    }
  }

  List<String> getCommaSeparatedValues(String name) {
    String value = config.get(name);
    if (value == null) {
      return Collections.emptyList();
    }
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  Map<String, String> getCommaSeparatedMap(String name) {
    return getCommaSeparatedValues(name).stream()
        .map(
            keyValuePair ->
                Arrays.stream(keyValuePair.split("=", 2))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()))
        .map(
            splitKeyValuePairs -> {
              if (splitKeyValuePairs.size() != 2) {
                throw new ConfigurationException(
                    "Map property key missing value: " + name + "=" + config.get(name));
              }
              return new AbstractMap.SimpleImmutableEntry<>(
                  splitKeyValuePairs.get(0), splitKeyValuePairs.get(1));
            })
        // If duplicate keys, prioritize later ones similar to duplicate system properties on a
        // Java command line.
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (first, next) -> next, LinkedHashMap::new));
  }

  boolean getBoolean(String name) {
    return Boolean.parseBoolean(config.get(name));
  }

  private static ConfigurationException newInvalidPropertyException(
      String name, String value, String type) {
    throw new ConfigurationException(
        "Invalid value for property " + name + "=" + value + ". Must be a " + type + ".");
  }
}
