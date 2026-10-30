/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.spi.DeclarativeConfigurationMapper;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class DeclarativeConfigurationMapperUtil {

  private static final DeclarativeConfigurationMapper MAPPER =
      DeclarativeConfigProviderLoader.load(
          DeclarativeConfigurationMapper.class, MissingMapper::new);

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
    return MAPPER.convert(value, targetType);
  }

  /**
   * Convert a model or parsed configuration value to a generic map using the selected mapper.
   *
   * @param value the value to convert
   * @return the converted map, or null if the mapper returns null
   */
  @Nullable
  public static Map<String, Object> toMap(Object value) {
    return MAPPER.toMap(value);
  }

  private static final class MissingMapper implements DeclarativeConfigurationMapper {

    @Override
    public <T> T convert(Object value, Class<T> targetType) {
      throw missingMapperException();
    }

    @Nullable
    @Override
    public Map<String, Object> toMap(Object value) {
      throw missingMapperException();
    }

    private static DeclarativeConfigException missingMapperException() {
      return new DeclarativeConfigException(
          "No DeclarativeConfigurationMapper provider is available. Add the optional "
              + "opentelemetry-sdk-extension-declarative-config-jackson artifact or register a "
              + "custom mapper provider.");
    }
  }
}
