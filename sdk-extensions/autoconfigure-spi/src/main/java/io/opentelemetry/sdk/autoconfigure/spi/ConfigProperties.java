/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Properties used for auto-configuration of the OpenTelemetry SDK components. */
public interface ConfigProperties {

  /**
   * Returns a string-valued configuration property.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  @Nullable
  String getString(String name);

  /**
   * @return a string-valued configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default String getString(String name, String defaultValue) {
    return _defaultIfNull(getString(name), defaultValue);
  }

  /**
   * Returns a boolean-valued configuration property. Implementations should use the same rules as
   * {@link Boolean#parseBoolean(String)} for handling the values.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid boolean.
   */
  @Nullable
  Boolean getBoolean(String name);

  /**
   * @return a Boolean-valued configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default Boolean getBoolean(String name, Boolean defaultValue) {
    return _defaultIfNull(getBoolean(name), defaultValue);
  }

  /**
   * Returns an Integer-valued configuration property.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid integer.
   */
  @Nullable
  Integer getInt(String name);

  /**
   * @return an Integer-valued configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default Integer getInt(String name, Integer defaultValue) {
    return _defaultIfNull(getInt(name), defaultValue);
  }

  /**
   * Returns a Long-valued configuration property.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid long.
   */
  @Nullable
  Long getLong(String name);

  /**
   * @return a Long-valued configuration property or {@code defaultValue} if a property with {@code
   *     name} has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default Long getLong(String name, Long defaultValue) {
    return _defaultIfNull(getLong(name), defaultValue);
  }

  /**
   * Returns a double-valued configuration property.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid double.
   */
  @Nullable
  Double getDouble(String name);

  /**
   * @return a Double-valued configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default Double getDouble(String name, Double defaultValue) {
    return _defaultIfNull(getDouble(name), defaultValue);
  }

  /**
   * Returns a duration property from the map, or {@code null} if it cannot be found or it has a
   * wrong type.
   *
   * <p>Durations can be of the form "{number}{unit}", where unit is one of:
   *
   * <ul>
   *   <li>ms
   *   <li>s
   *   <li>m
   *   <li>h
   *   <li>d
   * </ul>
   *
   * <p>If no unit is specified, milliseconds is the assumed duration unit.
   *
   * @param name The property name
   * @return the {@link Duration} value of the property, {@code null} if the property cannot be
   *     found.
   * @throws ConfigurationException for malformed duration strings.
   */
  @Nullable
  Duration getDuration(String name);

  /**
   * @see ConfigProperties#getDuration(String name)
   * @return a Double-valued configuration property or {@code defaultValue} if a property with name
   *     {@code name} has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default Duration getDuration(String name, Duration defaultValue) {
    return _defaultIfNull(getDuration(name), defaultValue);
  }

  /**
   * Returns a list-valued configuration property. The format of the original value must be
   * comma-separated. Empty values will be removed.
   *
   * @return an empty list if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid comma-separated list.
   */
  List<String> getList(String name);

  /**
   * @see ConfigProperties#getList(String name)
   * @return a List configuration property or {@code defaultValue} if a property with {@code name}
   *     has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default List<String> getList(String name, List<String> defaultValue) {
    List<String> value = getList(name);
    return (value == null || value.isEmpty()) ? defaultValue : value;
  }

  /**
   * Returns a Map configuration property. The format of the original value must be comma-separated
   * for each key, with an '=' separating the key and value. For instance, <code>
   * service.name=Greatest Service,host.name=localhost</code> Empty values will be removed.
   *
   * @return an empty map if the property has not been configured.
   * @throws ConfigurationException for malformed map strings.
   */
  Map<String, String> getMap(String name);

  /**
   * @see ConfigProperties#getMap(String name)
   * @return a Double-valued configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default Map<String, String> getMap(String name, Map<String, String> defaultValue) {
    Map<String, String> value = getMap(name);
    return (value == null || value.isEmpty()) ? defaultValue : value;
  }

  /**
   * Returns defaultValue if value is null, otherwise value. This is an internal method that should
   * not be broadly used.
   */
  default <T> T _defaultIfNull(@Nullable T value, T defaultValue) {
    return value == null ? defaultValue : value;
  }
}
