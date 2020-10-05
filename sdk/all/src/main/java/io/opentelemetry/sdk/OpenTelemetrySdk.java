/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.internal.Obfuscated;
import io.opentelemetry.sdk.baggage.BaggageManagerSdk;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides a static global accessor for SDK telemetry objects {@link
 * TracerSdkManagement}, {@link MeterSdkProvider} and {@link BaggageManagerSdk}.
 *
 * <p>This is a convenience class getting and casting the telemetry objects from {@link
 * OpenTelemetry}.
 *
 * @see OpenTelemetry
 */
@ThreadSafe
public final class OpenTelemetrySdk {
  /**
   * Returns a {@link TracerSdkManagement}.
   *
   * @return TracerSdkManagement for managing your Tracing SDK.
   */
  public static TracerSdkManagement getTracerManagement() {
    return (TracerSdkManagement) ((Obfuscated<?>) OpenTelemetry.getTracerProvider()).unobfuscate();
  }

  /**
   * Returns a {@link MeterSdkProvider}.
   *
   * @return MeterProvider returned by {@link OpenTelemetry#getMeterProvider()}.
   */
  public static MeterSdkProvider getMeterProvider() {
    return (MeterSdkProvider) OpenTelemetry.getMeterProvider();
  }

  /**
   * Returns a {@link BaggageManagerSdk}.
   *
   * @return context manager returned by {@link OpenTelemetry#getBaggageManager()}.
   */
  public static BaggageManagerSdk getBaggageManager() {
    return (BaggageManagerSdk) OpenTelemetry.getBaggageManager();
  }

  private OpenTelemetrySdk() {}
}
