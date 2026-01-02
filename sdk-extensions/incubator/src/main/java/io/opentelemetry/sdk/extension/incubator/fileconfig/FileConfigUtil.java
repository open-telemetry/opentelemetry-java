/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import java.util.Set;
import javax.annotation.Nullable;

final class FileConfigUtil {

  private FileConfigUtil() {}

  static <T> T requireNonNull(@Nullable T object, String description) {
    if (object == null) {
      throw new DeclarativeConfigException(description + " is required but is null");
    }
    return object;
  }

  static ConfigKeyValue validateSingleKeyValue(
      DeclarativeConfigContext context, Object model, String resourceName) {
    DeclarativeConfigProperties modelConfigProperties =
        DeclarativeConfiguration.toConfigProperties(
            model, context.getSpiHelper().getComponentLoader());
    Set<String> propertyKeys = modelConfigProperties.getPropertyKeys();
    if (propertyKeys.size() != 1) {
      String suffix =
          propertyKeys.isEmpty()
              ? ""
              : ": " + propertyKeys.stream().collect(joining(",", "[", "]"));
      throw new DeclarativeConfigException(
          resourceName + " must have exactly one entry but has " + propertyKeys.size() + suffix);
    }
    String key = propertyKeys.iterator().next();
    DeclarativeConfigProperties value = modelConfigProperties.getStructured(key);
    return ConfigKeyValue.of(key, value == null ? DeclarativeConfigProperties.empty() : value);
  }
}
