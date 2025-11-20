/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import java.util.Map;
import javax.annotation.Nullable;

final class FileConfigUtil {

  private FileConfigUtil() {}

  static <T> T assertNotNull(@Nullable T object, String description) {
    if (object == null) {
      throw new NullPointerException(description + " is null");
    }
    return object;
  }

  static <T> T requireNonNull(@Nullable T object, String description) {
    if (object == null) {
      throw new DeclarativeConfigException(description + " is required but is null");
    }
    return object;
  }

  static <T> Map.Entry<String, T> getSingletonMapEntry(
      Map<String, T> additionalProperties, String resourceName) {
    if (additionalProperties.isEmpty()) {
      throw new DeclarativeConfigException(resourceName + " must be set");
    }
    if (additionalProperties.size() > 1) {
      throw new DeclarativeConfigException(
          "Invalid configuration - multiple "
              + resourceName
              + "s set: "
              + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
    }
    return additionalProperties.entrySet().stream()
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Missing " + resourceName + ". This is a programming error."));
  }

  static void requireNullResource(
      @Nullable Object resource, String resourceName, Map<String, ?> additionalProperties) {
    if (resource != null) {
      throw new DeclarativeConfigException(
          "Invalid configuration - multiple "
              + resourceName
              + "s set: "
              + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
    }
  }
}
