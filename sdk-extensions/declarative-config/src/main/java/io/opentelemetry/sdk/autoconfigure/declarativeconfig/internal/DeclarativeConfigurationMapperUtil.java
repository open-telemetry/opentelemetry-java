/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal;

import io.opentelemetry.api.config.DeclarativeConfigException;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class DeclarativeConfigurationMapperUtil {

  private DeclarativeConfigurationMapperUtil() {}

  /**
   * Convert a value to an instance of {@code targetType} using the selected mapper.
   *
   * @param value the value to convert
   * @param targetType the target type
   * @param <T> the target type
   * @return the converted value
   */
  public static <T> T convert(Object value, Class<T> targetType) {
    try {
      return DeclarativeConfigProviderLoader.getMapper().convert(value, targetType);
    } catch (DeclarativeConfigException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new DeclarativeConfigException("Unable to convert value to " + targetType, e);
    }
  }

  /**
   * Convert a model or parsed configuration value to a generic map using the selected mapper.
   *
   * @param value the value to convert
   * @return the converted map, or null if the mapper returns null
   */
  @Nullable
  public static Map<String, Object> toMap(Object value) {
    try {
      return DeclarativeConfigProviderLoader.getMapper().toMap(value);
    } catch (DeclarativeConfigException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new DeclarativeConfigException("Unable to convert value to a map", e);
    }
  }
}
