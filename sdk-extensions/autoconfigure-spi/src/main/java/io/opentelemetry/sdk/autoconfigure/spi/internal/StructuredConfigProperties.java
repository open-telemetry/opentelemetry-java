/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import static io.opentelemetry.api.internal.ConfigUtil.defaultIfNull;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * An interface for accessing structured configuration data.
 *
 * <p>An instance of {@link StructuredConfigProperties} is equivalent to a <a
 * href="https://yaml.org/spec/1.2.2/#3211-nodes">YAML mapping node</a>. It has accessors for
 * reading scalar properties, {@link #getStructured(String)} for reading children which are
 * themselves mappings, and {@link #getStructuredList(String)} for reading children which are
 * sequences of mappings.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface StructuredConfigProperties {

  /**
   * Return an empty {@link StructuredConfigProperties} instance.
   *
   * <p>Useful for walking the tree without checking for null. For example, to access a string key
   * nested at .foo.bar.baz, call: {@code config.getStructured("foo", empty()).getStructured("bar",
   * empty()).getString("baz")}.
   */
  static StructuredConfigProperties empty() {
    return EmptyStructuredConfigProperties.getInstance();
  }

  /**
   * Returns a {@link String} configuration property.
   *
   * @return null if the property has not been configured
   * @throws ConfigurationException if the property is not a valid scalar string
   */
  @Nullable
  String getString(String name);

  /**
   * Returns a {@link String} configuration property.
   *
   * @return a {@link String} configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured
   * @throws ConfigurationException if the property is not a valid scalar string
   */
  default String getString(String name, String defaultValue) {
    return defaultIfNull(getString(name), defaultValue);
  }

  /**
   * Returns a {@link Boolean} configuration property. Implementations should use the same rules as
   * {@link Boolean#parseBoolean(String)} for handling the values.
   *
   * @return null if the property has not been configured
   * @throws ConfigurationException if the property is not a valid scalar boolean
   */
  @Nullable
  Boolean getBoolean(String name);

  /**
   * Returns a {@link Boolean} configuration property.
   *
   * @return a {@link Boolean} configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured
   * @throws ConfigurationException if the property is not a valid scalar boolean
   */
  default boolean getBoolean(String name, boolean defaultValue) {
    return defaultIfNull(getBoolean(name), defaultValue);
  }

  /**
   * Returns a {@link Integer} configuration property.
   *
   * <p>If the underlying config property is {@link Long}, it is converted to {@link Integer} with
   * {@link Long#intValue()} which may result in loss of precision.
   *
   * @return null if the property has not been configured
   * @throws ConfigurationException if the property is not a valid scalar integer
   */
  @Nullable
  Integer getInt(String name);

  /**
   * Returns a {@link Integer} configuration property.
   *
   * <p>If the underlying config property is {@link Long}, it is converted to {@link Integer} with
   * {@link Long#intValue()} which may result in loss of precision.
   *
   * @return a {@link Integer} configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured
   * @throws ConfigurationException if the property is not a valid scalar integer
   */
  default int getInt(String name, int defaultValue) {
    return defaultIfNull(getInt(name), defaultValue);
  }

  /**
   * Returns a {@link Long} configuration property.
   *
   * @return null if the property has not been configured
   * @throws ConfigurationException if the property is not a valid scalar long
   */
  @Nullable
  Long getLong(String name);

  /**
   * Returns a {@link Long} configuration property.
   *
   * @return a {@link Long} configuration property or {@code defaultValue} if a property with {@code
   *     name} has not been configured
   * @throws ConfigurationException if the property is not a valid scalar long
   */
  default long getLong(String name, long defaultValue) {
    return defaultIfNull(getLong(name), defaultValue);
  }

  /**
   * Returns a {@link Double} configuration property.
   *
   * @return null if the property has not been configured
   * @throws ConfigurationException if the property is not a valid scalar double
   */
  @Nullable
  Double getDouble(String name);

  /**
   * Returns a {@link Double} configuration property.
   *
   * @return a {@link Double} configuration property or {@code defaultValue} if a property with
   *     {@code name} has not been configured
   * @throws ConfigurationException if the property is not a valid scalar double
   */
  default double getDouble(String name, double defaultValue) {
    return defaultIfNull(getDouble(name), defaultValue);
  }

  /**
   * Returns a {@link List} configuration property. Empty values and values which do not map to the
   * {@code scalarType} will be removed.
   *
   * @param name the property name
   * @param scalarType the scalar type, one of {@link String}, {@link Boolean}, {@link Long} or
   *     {@link Double}
   * @return a {@link List} configuration property, or null if the property has not been configured
   * @throws ConfigurationException if the property is not a valid sequence of scalars, or if {@code
   *     scalarType} is not supported
   */
  @Nullable
  <T> List<T> getScalarList(String name, Class<T> scalarType);

  /**
   * Returns a {@link List} configuration property. Entries which are not strings are converted to
   * their string representation.
   *
   * @see ConfigProperties#getList(String name)
   * @return a {@link List} configuration property or {@code defaultValue} if a property with {@code
   *     name} has not been configured
   * @throws ConfigurationException if the property is not a valid sequence of scalars
   */
  default <T> List<T> getScalarList(String name, Class<T> scalarType, List<T> defaultValue) {
    return defaultIfNull(getScalarList(name, scalarType), defaultValue);
  }

  /**
   * Returns a {@link StructuredConfigProperties} configuration property.
   *
   * @return a map-valued configuration property, or {@code null} if {@code name} has not been
   *     configured
   * @throws ConfigurationException if the property is not a mapping
   */
  @Nullable
  StructuredConfigProperties getStructured(String name);

  /**
   * Returns a {@link StructuredConfigProperties} configuration property.
   *
   * @return a map-valued configuration property, or {@code defaultValue} if {@code name} has not
   *     been configured
   * @throws ConfigurationException if the property is not a mapping
   */
  default StructuredConfigProperties getStructured(
      String name, StructuredConfigProperties defaultValue) {
    return defaultIfNull(getStructured(name), defaultValue);
  }

  /**
   * Returns a list of {@link StructuredConfigProperties} configuration property.
   *
   * @return a list of map-valued configuration property, or {@code null} if {@code name} has not
   *     been configured
   * @throws ConfigurationException if the property is not a sequence of mappings
   */
  @Nullable
  List<StructuredConfigProperties> getStructuredList(String name);

  /**
   * Returns a list of {@link StructuredConfigProperties} configuration property.
   *
   * @return a list of map-valued configuration property, or {@code defaultValue} if {@code name}
   *     has not been configured
   * @throws ConfigurationException if the property is not a sequence of mappings
   */
  default List<StructuredConfigProperties> getStructuredList(
      String name, List<StructuredConfigProperties> defaultValue) {
    return defaultIfNull(getStructuredList(name), defaultValue);
  }

  /**
   * Returns a set of all configuration property keys.
   *
   * @return the configuration property keys
   */
  Set<String> getPropertyKeys();
}
