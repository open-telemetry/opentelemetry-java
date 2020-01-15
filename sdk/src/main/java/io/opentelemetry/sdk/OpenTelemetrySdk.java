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
import io.opentelemetry.sdk.metrics.MeterSdkRegistry;
import io.opentelemetry.sdk.trace.TracerSdkRegistry;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class provides a static global accessor for SDK telemetry objects {@link TracerSdkRegistry},
 * {@link MeterSdkRegistry} and {@link CorrelationContextManagerSdk}.
 *
 * <p>This is a convenience class getting and casting the telemetry objects from {@link
 * OpenTelemetry}.
 *
 * @see OpenTelemetry
 */
@ThreadSafe
public final class OpenTelemetrySdk {
  /**
   * Returns a {@link TracerSdkRegistry}.
   *
   * @return TracerRegistry returned by {@link OpenTelemetry#getTracerRegistry()}.
   * @since 0.1.0
   */
  public static TracerSdkRegistry getTracerRegistry() {
    return (TracerSdkRegistry) OpenTelemetry.getTracerRegistry();
  }

  /**
   * Returns a {@link MeterSdkRegistry}.
   *
   * @return MeterRegistry returned by {@link OpenTelemetry#getMeterRegistry()}.
   * @since 0.1.0
   */
  public static MeterSdkRegistry getMeterRegistry() {
    return (MeterSdkRegistry) OpenTelemetry.getMeterRegistry();
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
