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

/**
 * Properties to be used for auto-configuration of the OpenTelemetry SDK components. These
 * properties will be a combination of system properties and environment variables. The properties
 * for both of these will be normalized to be all lower case, and underscores will be replaced with
 * periods.
 */
public class ConfigProperties {

  private final Map<String, String> config;

  static ConfigProperties get() {
    return new ConfigProperties(System.getProperties(), System.getenv());
  }

  // Visible for testing
  static ConfigProperties createForTest(Map<String, String> properties) {
    return new ConfigProperties(properties, Collections.emptyMap());
  }

  private ConfigProperties(Map<?, ?> systemProperties, Map<String, String> environmentVariables) {
    Map<String, String> config = new HashMap<>();
    environmentVariables.forEach(
        (name, value) -> config.put(name.toLowerCase(Locale.ROOT).replace('_', '.'), value));
    systemProperties.forEach(
        (key, value) -> config.put(((String) key).toLowerCase(Locale.ROOT), (String) value));

    this.config = config;
  }

  /**
   * Returns a string-valued configuration property.
   *
   * @return null if the property has not been configured.
   */
  @Nullable
  public String getString(String name) {
    return config.get(name);
  }

  /**
   * Returns a integer-valued configuration property.
   *
   * @return null if the property has not been configured.
   * @throws NumberFormatException if the property is not a valid integer.
   */
  @Nullable
  @SuppressWarnings("UnusedException")
  public Integer getInt(String name) {
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

  /**
   * Returns a long-valued configuration property.
   *
   * @return null if the property has not been configured.
   * @throws NumberFormatException if the property is not a valid long.
   */
  @Nullable
  @SuppressWarnings("UnusedException")
  public Long getLong(String name) {
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

  /**
   * Returns a double-valued configuration property.
   *
   * @return null if the property has not been configured.
   * @throws NumberFormatException if the property is not a valid double.
   */
  @Nullable
  @SuppressWarnings("UnusedException")
  public Double getDouble(String name) {
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

  /**
   * Returns a list-valued configuration property. The format of the original value must be
   * comma-separated. Empty values will be removed.
   *
   * @return an empty list if the property has not been configured.
   */
  public List<String> getCommaSeparatedValues(String name) {
    String value = config.get(name);
    if (value == null) {
      return Collections.emptyList();
    }
    return filterBlanksAndNulls(value.split(","));
  }

  /**
   * Returns a map-valued configuration property. The format of the original value must be
   * comma-separated for each key, with an '=' separating the key and value. For instance, <code>
   * service.name=Greatest Service,host.name=localhost</code> Empty values will be removed.
   *
   * @return an empty list if the property has not been configured.
   */
  public Map<String, String> getCommaSeparatedMap(String name) {
    return getCommaSeparatedValues(name).stream()
        .map(keyValuePair -> filterBlanksAndNulls(keyValuePair.split("=", 2)))
        .map(
            splitKeyValuePairs -> {
              if (splitKeyValuePairs.size() != 2) {
                throw new ConfigurationException(
                    "Invalid map property: " + name + "=" + config.get(name));
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

  /**
   * Returns a boolean-valued configuration property. Uses the same rules as {@link
   * Boolean#parseBoolean(String)} for handling the values.
   *
   * @return false if the property has not been configured.
   */
  public boolean getBoolean(String name) {
    return Boolean.parseBoolean(config.get(name));
  }

  private static ConfigurationException newInvalidPropertyException(
      String name, String value, String type) {
    throw new ConfigurationException(
        "Invalid value for property " + name + "=" + value + ". Must be a " + type + ".");
  }

  private static List<String> filterBlanksAndNulls(String[] values) {
    return Arrays.stream(values)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }
}
