/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export.metrics;

import io.opentelemetry.api.metrics.MeterProvider;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public interface SpanProcessorMetrics extends AutoCloseable {

  String QUEUE_FULL_DROPPED_REASON = "queue_full";

  static SpanProcessorMetrics noop() {
    return new NoopSpanProcessorMetrics();
  }

  static SpanProcessorMetrics legacyBatchProcessorMetrics(Supplier<MeterProvider> meterProvider) {
    return new LegacyBatchSpanProcessorMetrics(meterProvider);
  }

  void recordSpansProcessed(long count, @Nullable String errorType);

  void recordSpansExportedSuccessfully(long count);

  /** Must be called at most once. */
  void startRecordingQueueMetrics(
      LongSupplier queueSizeSupplier, LongSupplier queueCapacitySupplier);

  /**
   * Must be called if {@link #startRecordingQueueMetrics(LongSupplier, LongSupplier)} was called,
   * otherwise not required.
   */
  @Override
  void close();
}
