/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.function.Supplier;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public interface SpanInstrumentation {

  static SpanInstrumentation create(
      InternalTelemetryVersion internalTelemetryVersion,
      Supplier<MeterProvider> meterProviderSupplier) {
    switch (internalTelemetryVersion) {
      case LEGACY:
        return NoopSpanInstrumentation.INSTANCE;
      case LATEST:
        return new SemConvSpanInstrumentation(meterProviderSupplier);
    }
    throw new IllegalStateException(
        "Unhandled telemetry schema version: " + internalTelemetryVersion);
  }

  Recording recordSpanStart(SamplingResult samplingResult, SpanContext parentSpanContext);

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  interface Recording {

    boolean isNoop();

    void recordSpanEnd();
  }
}
