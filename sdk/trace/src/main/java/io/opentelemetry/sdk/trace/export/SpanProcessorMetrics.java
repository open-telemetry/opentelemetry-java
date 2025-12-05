/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import javax.annotation.Nullable;

/** Metrics exported by span processors. */
interface SpanProcessorMetrics {

  static SpanProcessorMetrics get(
      InternalTelemetryVersion telemetryVersion,
      ComponentId componentId,
      MeterProvider meterProvider) {
    switch (telemetryVersion) {
      case LEGACY:
        return new LegacySpanProcessorMetrics(meterProvider);
      default:
        return new SemConvSpanProcessorMetrics(componentId, meterProvider);
    }
  }

  /** Records metrics for spans dropped because a queue is full. */
  void dropSpans(int count);

  /** Record metrics for spans processed, possibly with an error. */
  void finishSpans(int count, @Nullable String error);

  /** Registers a metric for processor queue capacity. */
  void buildQueueCapacityMetric(long capacity);

  interface LongCallable {
    long get();
  }

  /** Registers a metric for processor queue size. */
  void buildQueueSizeMetric(LongCallable queueSize);
}
