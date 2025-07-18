/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import io.opentelemetry.api.incubator.ExtendedOpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.WithShutdown;
import java.lang.reflect.Field;
import javax.annotation.Nullable;

/** A new interface for creating OpenTelemetrySdk that supports getting {@link ConfigProvider}. */
public interface ExtendedOpenTelemetrySdk extends ExtendedOpenTelemetry, WithShutdown {
  /** Returns a builder for {@link ExtendedOpenTelemetrySdk}. */
  static ExtendedOpenTelemetrySdkBuilder builder() {
    return new ExtendedOpenTelemetrySdkBuilder();
  }

  @Nullable
  static ExtendedOpenTelemetrySdk fromOpenTelemetrySdk(OpenTelemetrySdk openTelemetry) {
    try {
      Class<?> sdk = Class.forName("io.opentelemetry.sdk.OpenTelemetrySdk");
      Field extendedOpenTelemetrySdk = sdk.getDeclaredField("extendedOpenTelemetrySdk");
      extendedOpenTelemetrySdk.setAccessible(true);
      return (ExtendedOpenTelemetrySdk) extendedOpenTelemetrySdk.get(openTelemetry);
    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException(
          "Cannot create ExtendedOpenTelemetrySdk from OpenTelemetrySdk", e);
    }
  }
}
