/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Converts parsed configuration values to declarative configuration models and generic maps.
 *
 * <p>Implementations are discovered with {@link java.util.ServiceLoader}. The implementation with
 * the highest {@link #order()} is used. The optional {@code
 * opentelemetry-sdk-extension-declarative-config-jackson} artifact supplies a Jackson 2
 * implementation. Implementations must preserve the mapping behavior of the declarative
 * configuration models, including extension properties and null values.
 */
public interface DeclarativeConfigurationMapper extends Ordered {

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
