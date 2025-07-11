/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator;

import io.opentelemetry.api.incubator.ExtendedOpenTelemetry;
import io.opentelemetry.api.incubator.entities.EntityProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.io.Closeable;

/** A new interface for creating OpenTelemetrySdk that supports {@link EntityProvider}. */
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
}
