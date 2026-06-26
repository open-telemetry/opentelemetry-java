/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.common.export.JsonProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for loading {@link JsonProvider} implementations.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class JsonProviderUtil {

  private static final Logger LOGGER = Logger.getLogger(JsonProviderUtil.class.getName());

  private static final String JSON_SPI_PROPERTY = "io.opentelemetry.sdk.common.export.JsonProvider";

  private JsonProviderUtil() {}

  /**
   * Resolve the {@link JsonProvider}.
   *
   * <p>If no {@link JsonProvider} is available, throw {@link IllegalStateException}.
   *
   * <p>If only one {@link JsonProvider} is available, use it.
   *
   * <p>If multiple are available and..
   *
   * <ul>
   *   <li>{@code io.opentelemetry.sdk.common.export.JsonProvider} is empty, use the first found.
   *   <li>{@code io.opentelemetry.sdk.common.export.JsonProvider} is set, use the matching
   *       provider. If none match, throw {@link IllegalStateException}.
   * </ul>
   */
  public static JsonProvider resolveJsonProvider(ComponentLoader componentLoader) {
    Map<String, JsonProvider> jsonProviders = new HashMap<>();
    for (JsonProvider spi : componentLoader.load(JsonProvider.class)) {
      jsonProviders.put(spi.getClass().getName(), spi);
    }

    // No provider on classpath, throw
    if (jsonProviders.isEmpty()) {
      throw new IllegalStateException(
          "No JsonProvider found on classpath. Please add dependency on "
              + "opentelemetry-json-jackson-2 or opentelemetry-json-jackson-3");
    }

    // Exactly one provider on classpath, use it
    if (jsonProviders.size() == 1) {
      return jsonProviders.values().stream().findFirst().get();
    }

    // If we've reached here, there are multiple JsonProviders
    String configuredProvider = ConfigUtil.getString(JSON_SPI_PROPERTY, "");

    // Multiple providers but none configured, use first we find and log a warning
    if (configuredProvider.isEmpty()) {
      LOGGER.log(
          Level.WARNING,
          "Multiple JsonProvider found. Please include only one, "
              + "or specify preference setting "
              + JSON_SPI_PROPERTY
              + " to the FQCN of the preferred provider.");
      return jsonProviders.values().stream().findFirst().get();
    }

    // Multiple providers with configuration match, use configuration match
    if (jsonProviders.containsKey(configuredProvider)) {
      return jsonProviders.get(configuredProvider);
    }

    // Multiple providers, configured does not match, throw
    throw new IllegalStateException(
        "No JsonProvider matched configured " + JSON_SPI_PROPERTY + ": " + configuredProvider);
  }
}
