/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.jackson.internal;

import com.fasterxml.jackson.core.type.TypeReference;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.spi.DeclarativeConfigurationMapper;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class JacksonDeclarativeConfigurationMapper implements DeclarativeConfigurationMapper {

  @Override
  public int order() {
    return Integer.MIN_VALUE;
  }

  @Override
  public <T> T convert(Object value, Class<T> targetType) {
    return JacksonModelMapper.MAPPER.convertValue(value, targetType);
  }

  @Nullable
  @Override
  public Map<String, Object> toMap(Object value) {
    return JacksonModelMapper.MAPPER.convertValue(
        value, new TypeReference<Map<String, Object>>() {});
  }
}
