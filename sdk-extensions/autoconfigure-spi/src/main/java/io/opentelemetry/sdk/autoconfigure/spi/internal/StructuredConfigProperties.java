/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import static io.opentelemetry.api.internal.ConfigUtil.defaultIfNull;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.util.List;
import javax.annotation.Nullable;

/**
 * An interface from accessing structured configuration data.
 *
 * <p>In addition to accessors for reading primitive properties which are direct children, {@link
 * StructuredConfigProperties} has {@link #getStructured(String)} and {@link
 * #getStructuredList(String)} for reading complex children which are themselves structured.
 */
public interface StructuredConfigProperties {

  /**
   * Returns a {@link String} configuration property.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  @Nullable
  String getString(String name);

  /**
   * Returns a {@link String} configuration property.
   *
   * @return a {@link String} configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   * @since 1.15.0
   */
  default String getString(String name, String defaultValue) {
    return defaultIfNull(getString(name), defaultValue);
  }

  /**
   * Returns a {@link Boolean} configuration property. Implementations should use the same rules as
   * {@link Boolean#parseBoolean(String)} for handling the values.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid boolean.
   */
  @Nullable
  Boolean getBoolean(String name);

  /**
   * Returns a {@link Boolean} configuration property.
   *
   * @return a {@link Boolean} configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default boolean getBoolean(String name, boolean defaultValue) {
    return defaultIfNull(getBoolean(name), defaultValue);
  }

  /**
   * Returns a {@link Long} configuration property.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid long.
   */
  @Nullable
  Long getLong(String name);

  /**
   * Returns a {@link Long} configuration property.
   *
   * @return a {@link Long} configuration property or {@code defaultValue} if a property with {@code
   *     name} has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default long getLong(String name, long defaultValue) {
    return defaultIfNull(getLong(name), defaultValue);
  }

  /**
   * Returns a {@link Double} configuration property.
   *
   * @return null if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid double.
   */
  @Nullable
  Double getDouble(String name);

  /**
   * Returns a {@link Double} configuration property.
   *
   * @return a {@link Double} configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured.
   * @throws ConfigurationException if the property is not a valid double.
   */
  default double getDouble(String name, double defaultValue) {
    return defaultIfNull(getDouble(name), defaultValue);
  }

  /**
   * Returns a {@link List} configuration property. Empty values will be removed. Entries which are
   * not strings are converted to their string representation.
   *
   * @return an empty list if the property has not been configured.
   * @throws ConfigurationException if the property is not a valid comma-separated list.
   */
  // TODO(jack-berg): Should we differentiate between empty and null?
  List<String> getPrimitiveList(String name);

  /**
   * Returns a {@link List} value configuration property. Entries which are not strings are
   * converted to their string representation.
   *
   * @see ConfigProperties#getList(String name)
   * @return a List configuration property or {@code defaultValue} if a property with {@code name}
   *     has not been configured.
   * @throws ConfigurationException if the property is not a valid string.
   */
  default List<String> getPrimitiveList(String name, List<String> defaultValue) {
    List<String> value = getPrimitiveList(name);
    return value.isEmpty() ? defaultValue : value;
  }

  /**
   * Returns the {@link StructuredConfigProperties} for the given property {@code name}.
   *
   * @return a map-valued configuration property, or {@code null} if {@code name} has not been
   *     configured.
   * @throws io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException if the property is not a
   *     map
   */
  @Nullable
  StructuredConfigProperties getStructured(String name);

  /**
   * Returns a list of {@link StructuredConfigProperties} for the given property.
   *
   * @return a list of map-valued configuration property, or {@code null} if {@code name} has not
   *     been configured.
   * @throws io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException if the property is not a
   *     list of maps
   */
  @Nullable
  List<StructuredConfigProperties> getStructuredList(String name);
}
