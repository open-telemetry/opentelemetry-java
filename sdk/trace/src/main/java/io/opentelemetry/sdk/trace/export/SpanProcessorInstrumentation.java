/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/** Metrics exported by span processors. */
interface SpanProcessorInstrumentation {

  static SpanProcessorInstrumentation get(
      InternalTelemetryVersion telemetryVersion,
      ComponentId componentId,
      Supplier<MeterProvider> meterProvider) {
    switch (telemetryVersion) {
      case LEGACY:
        return new LegacySpanProcessorInstrumentation(meterProvider);
      default:
        return new SemConvSpanProcessorInstrumentation(componentId, meterProvider);
    }
  }

  /** Records metrics for spans dropped because a queue is full. */
  void dropSpans(int count);

  /** Record metrics for spans processed, possibly with an error. */
  void finishSpans(int count, @Nullable String error);

  /** Registers metrics for processor queue capacity and size. */
  void buildQueueMetricsOnce(long capacity, LongCallable getSize);
}
