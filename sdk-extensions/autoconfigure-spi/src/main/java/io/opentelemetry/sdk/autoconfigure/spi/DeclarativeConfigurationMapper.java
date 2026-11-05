/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import java.util.Map;
import javax.annotation.Nullable;

/**
 * Converts parsed configuration values to declarative configuration models and generic maps.
 *
 * <p>Implementations are discovered with {@link java.util.ServiceLoader}. If exactly one
 * implementation is available, it is used. If multiple implementations are available, set a system
 * property or environment variable whose name is this interface's fully qualified class name and
 * whose value is the fully qualified class name of the desired implementation. If no implementation
 * is selected, the first discovered implementation is used and a warning is logged. The Jackson 2
 * implementation is supplied by the {@code opentelemetry-sdk-extension-declarative-config-jackson2}
 * runtime adapter, which is included by default with declarative config and can be excluded when
 * using a custom mapper. Implementations must preserve the mapping behavior of the declarative
 * configuration models, including extension properties and null values.
 */
public interface DeclarativeConfigurationMapper {

  /**
   * Convert a value to an instance of {@code targetType}.
   *
   * @param value the value to convert
   * @param targetType the target type
   * @param <T> the target type
   * @return the converted value
   */
  <T> T convert(Object value, Class<T> targetType);

  /**
   * Convert a model or parsed configuration value to a map with string keys.
   *
   * @param value the value to convert
   * @return the converted map, or null if the input value is null
   */
  @Nullable
  Map<String, Object> toMap(Object value);
}
