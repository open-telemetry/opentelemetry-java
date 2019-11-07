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
import io.opentelemetry.sdk.distributedcontext.CorrelationContextManagerSdk;
import io.opentelemetry.sdk.metrics.MeterSdk;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides a static global accessor for SDK telemetry objects {@link TracerSdk}, {@link
 * MeterSdk} and {@link CorrelationContextManagerSdk}.
 *
 * <p>This is a convenience class getting and casting the telemetry objects from {@link
 * OpenTelemetry}.
 *
 * @see OpenTelemetry
 */
@ThreadSafe
public final class OpenTelemetrySdk {
  /**
   * Returns a {@link TracerSdkFactory}.
   *
   * @return TracerFactory returned by {@link OpenTelemetry#getTracerFactory()}.
   * @since 0.1.0
   */
  public static TracerSdkFactory getTracerFactory() {
    return (TracerSdkFactory) OpenTelemetry.getTracerFactory();
  }

  /**
   * Returns a {@link MeterSdk}.
   *
   * @return meter returned by {@link OpenTelemetry#getMeter()}.
   * @since 0.1.0
   */
  public static MeterSdk getMeter() {
    return (MeterSdk) OpenTelemetry.getMeter();
  }

  /**
   * Returns a {@link CorrelationContextManagerSdk}.
   *
   * @return context manager returned by {@link OpenTelemetry#getCorrelationContextManager()}.
   * @since 0.1.0
   */
  public static CorrelationContextManagerSdk getDistributedContextManager() {
    return (CorrelationContextManagerSdk) OpenTelemetry.getCorrelationContextManager();
  }

  private OpenTelemetrySdk() {}
}
