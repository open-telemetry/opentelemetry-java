/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

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
   */
  @Nullable
  String getString(String name);

  /**
   * Returns a boolean-valued configuration property. Uses the same rules as {@link
   * Boolean#parseBoolean(String)} for handling the values.
   *
   * @return false if the property has not been configured.
   */
  boolean getBoolean(String name);

  /**
   * Returns a integer-valued configuration property.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid integer.
   */
  @Nullable
  Integer getInt(String name);

  /**
   * Returns a long-valued configuration property.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid long.
   */
  @Nullable
  Long getLong(String name);

  /**
   * Returns a double-valued configuration property.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid double.
   */
  @Nullable
  Double getDouble(String name);

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
   * Returns a list-valued configuration property. The format of the original value must be
   * comma-separated. Empty values will be removed.
   *
   * @return an empty list if the property has not been configured.
   */
  List<String> getCommaSeparatedValues(String name);

  /**
   * Returns a map-valued configuration property. The format of the original value must be
   * comma-separated for each key, with an '=' separating the key and value. For instance, <code>
   * service.name=Greatest Service,host.name=localhost</code> Empty values will be removed.
   *
   * @return an empty map if the property has not been configured.
   * @throws ConfigurationException for malformed map strings.
   */
  Map<String, String> getCommaSeparatedMap(String name);
}
