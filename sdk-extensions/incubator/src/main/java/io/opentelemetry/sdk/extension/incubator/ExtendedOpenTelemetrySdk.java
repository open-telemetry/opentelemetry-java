/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import io.opentelemetry.api.incubator.ExtendedOpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.lang.reflect.Field;

/** A new interface for creating OpenTelemetrySdk that supports getting {@link ConfigProvider}. */
public interface ExtendedOpenTelemetrySdk extends ExtendedOpenTelemetry, Closeable {
  /**
   * Shutdown the SDK. Calls {@link SdkTracerProvider#shutdown()}, {@link
   * SdkMeterProvider#shutdown()}, and {@link SdkLoggerProvider#shutdown()}.
   *
   * @return a {@link CompletableResultCode} which completes when all providers are shutdown
   */
  CompletableResultCode shutdown();

  /** Returns a builder for {@link ExtendedOpenTelemetrySdk}. */
  static ExtendedOpenTelemetrySdkBuilder builder() {
    return new ExtendedOpenTelemetrySdkBuilder();
  }

  @Nullable default ExtendedOpenTelemetrySdk fromOpenTelemetrySdk(OpenTelemetrySdk openTelemetry) {
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
