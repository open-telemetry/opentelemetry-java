/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/** Metrics exported by span processors. */
interface LogRecordProcessorInstrumentation {

  static LogRecordProcessorInstrumentation get(
      InternalTelemetryVersion telemetryVersion,
      ComponentId componentId,
      Supplier<MeterProvider> meterProvider) {
    switch (telemetryVersion) {
      case LEGACY:
        return new LegacyLogRecordProcessorInstrumentation(meterProvider);
      default:
        return new SemConvLogRecordProcessorInstrumentation(componentId, meterProvider);
    }
  }

  /** Records metrics for logs dropped because a queue is full. */
  void dropLogs(int count);

  /** Record metrics for logs processed, possibly with an error. */
  void finishLogs(int count, @Nullable String error);

  /** Registers metrics for processor queue capacity and size. */
  void buildQueueMetricsOnce(long capacity, LongCallable getSize);
}
