/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import java.util.Locale;
import javax.annotation.Nullable;

/**
 * Reads the desired SDK internal telemetry version from {@link ConfigProperties}.
 *
 * <p>This is the same as {@code
 * io.opentelemetry.exporter.otlp.internal.InternalTelemetryConfiguration}. Any changes should be
 * reflected there as well.
 */
final class InternalTelemetryConfiguration {

  @Nullable
  static InternalTelemetryVersion getVersion(ConfigProperties config) {
    String version = config.getString("otel.experimental.sdk.telemetry.version");
    if (version == null) {
      return null;
    }
    switch (version.toLowerCase(Locale.ROOT)) {
      case "legacy":
        return InternalTelemetryVersion.LEGACY;
      case "latest":
        return InternalTelemetryVersion.LATEST;
      default:
        throw new ConfigurationException("Invalid sdk telemetry version: " + version);
    }
  }

  private InternalTelemetryConfiguration() {}
}
