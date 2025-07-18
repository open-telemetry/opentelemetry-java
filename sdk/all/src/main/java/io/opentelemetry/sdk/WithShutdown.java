/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;

public interface WithShutdown extends Closeable {
  /**
   * Shutdown the SDK. Calls {@link SdkTracerProvider#shutdown()}, {@link
   * SdkMeterProvider#shutdown()}, and {@link SdkLoggerProvider#shutdown()}.
   *
   * @return a {@link CompletableResultCode} which completes when all providers are shutdown
   */
  CompletableResultCode shutdown();

  @Override
  default void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
