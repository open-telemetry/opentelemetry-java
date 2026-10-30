/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import java.util.Set;
import javax.annotation.Nullable;

final class FileConfigUtil {

  private FileConfigUtil() {}

  /**
   * Returns {@code object} if it is non-null, otherwise throw a {@link DeclarativeConfigException}
   * describing the missing {@code description}. The description is prefixed with the {@linkplain
   * DeclarativeConfigContext#getConfigPath() path} of the node currently being interpreted, so that
   * the error points to its location in the configuration document.
   */
  static <T> T requireNonNull(
      DeclarativeConfigContext context, @Nullable T object, String description) {
    if (object == null) {
      throw new DeclarativeConfigException(
          withConfigPath(context, description + " is required but is null"));
    }
    return object;
  }

  /**
   * Prefix {@code message} with the {@linkplain DeclarativeConfigContext#getConfigPath() path} of
   * the node currently being interpreted, so that errors point to their location in the
   * configuration document.
   */
  static String withConfigPath(DeclarativeConfigContext context, String message) {
    String path = context.getConfigPath();
    return path.isEmpty() ? message : path + " " + message;
  }

  static ConfigKeyValue validateSingleKeyValue(
      DeclarativeConfigContext context, Object model, String resourceName) {
    DeclarativeConfigProperties modelConfigProperties =
        DeclarativeConfiguration.toConfigProperties(model, context);
    Set<String> propertyKeys = modelConfigProperties.getPropertyKeys();
    if (propertyKeys.size() != 1) {
      String suffix =
          propertyKeys.isEmpty()
              ? ""
              : ": " + propertyKeys.stream().collect(joining(",", "[", "]"));
      throw new DeclarativeConfigException(
          withConfigPath(
              context,
              resourceName
                  + " must have exactly one entry but has "
                  + propertyKeys.size()
                  + suffix));
    }
    String key = propertyKeys.iterator().next();
    DeclarativeConfigProperties value = modelConfigProperties.getStructured(key);
    return ConfigKeyValue.of(key, value == null ? DeclarativeConfigProperties.empty() : value);
  }
}
