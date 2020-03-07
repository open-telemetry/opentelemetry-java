/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk;

import io.opentelemetry.OpenTelemetry;
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
    return (TracerSdkProvider) OpenTelemetry.getTracerProvider();
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
