/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

class ConfigProperties {

  static ConfigProperties get() {
    return new ConfigProperties(System.getProperties(), System.getenv());
  }

  private final Map<String, String> config;

  private ConfigProperties(Properties systemProperties, Map<String, String> environmentVariables) {
    Map<String, String> config = new HashMap<>();
    environmentVariables.forEach(
        (name, value) -> {
          config.put(name.toLowerCase(Locale.ROOT).replace('_', '.'), value);
        });
    systemProperties.forEach(
        (key, value) -> {
          config.put(((String) key).toLowerCase(Locale.ROOT), (String) value);
        });

    this.config = config;
  }

  @Nullable
  String getString(String name) {
    return config.get(name);
  }

  @Nullable
  Integer getInt(String name) {
    try {
      // Null will throw NumberFormatException too.
      return Integer.parseInt(config.get(name));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  @Nullable
  Long getLong(String name) {
    try {
      // Null will throw NumberFormatException too.
      return Long.parseLong(config.get(name));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Get a double property from the config or {@code null} if it cannot be found or it has a wrong
   * type.
   */
  @Nullable
  Double getDouble(String name) {
    try {
      return Double.parseDouble(config.get(name));
    } catch (NumberFormatException | NullPointerException ex) {
      return null;
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
            splitKeyValuePairs ->
                new AbstractMap.SimpleImmutableEntry<>(
                    splitKeyValuePairs.get(0), splitKeyValuePairs.get(1)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  boolean getBoolean(String name) {
    return Boolean.parseBoolean(config.get(name));
  }
}
