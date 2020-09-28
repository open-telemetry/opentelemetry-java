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
import io.opentelemetry.internal.Obfuscated;
import io.opentelemetry.sdk.baggage.BaggageManagerSdk;
import io.opentelemetry.sdk.metrics.MeterSdkProvider;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
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
