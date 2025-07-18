/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal.shutdown;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * Class that provides a shutdown method to close the SDK components gracefully.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
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
