/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.internal.Obfuscated;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides a static global accessor for SDK telemetry objects {@link TracerSdkProvider},
 * {@link MeterSdkProvider} and {@link CorrelationContextManagerSdk}.
 *
 * <p>This is a convenience class getting and casting the telemetry objects from {@link
 * OpenTelemetry}.
 *
 * @see OpenTelemetry
 */
@ThreadSafe
public final class OpenTelemetrySdk {
  /**
   * Returns a {@link TracerSdkProvider}.
   *
   * @return TracerProvider returned by {@link OpenTelemetry#getTracerProvider()}.
   * @since 0.1.0
   */
  public static TracerSdkProvider getTracerProvider() {
    return (TracerSdkProvider) ((Obfuscated<?>) OpenTelemetry.getTracerProvider()).unobfuscate();
  }

  /**
   * Returns a {@link MeterSdkProvider}.
   *
   * @return MeterProvider returned by {@link OpenTelemetry#getMeterProvider()}.
   * @since 0.1.0
   */
  public static MeterSdkProvider getMeterProvider() {
    return (MeterSdkProvider) OpenTelemetry.getMeterProvider();
  }

  /**
   * Returns a {@link CorrelationContextManagerSdk}.
   *
   * @return context manager returned by {@link OpenTelemetry#getCorrelationContextManager()}.
   * @since 0.1.0
   */
  public static CorrelationContextManagerSdk getCorrelationContextManager() {
    return (CorrelationContextManagerSdk) OpenTelemetry.getCorrelationContextManager();
  }

  private OpenTelemetrySdk() {}
}
