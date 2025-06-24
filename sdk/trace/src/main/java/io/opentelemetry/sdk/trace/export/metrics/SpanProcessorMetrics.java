/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export.metrics;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.StandardComponentId;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public interface SpanProcessorMetrics extends AutoCloseable {

  String QUEUE_FULL_DROPPED_REASON = "queue_full";

  static SpanProcessorMetrics noop() {
    return new NoopSpanProcessorMetrics();
  }

  static SpanProcessorMetrics createForBatchProcessor(
      InternalTelemetryVersion version, Supplier<MeterProvider> meterProvider) {
    switch (version) {
      case LEGACY:
        return new LegacyBatchSpanProcessorMetrics(meterProvider);
      case LATEST:
        return new SemConvSpanProcessorMetrics(
            meterProvider,
            ComponentId.generateLazy(StandardComponentId.SpanProcessorType.BATCH_SPAN_PROCESSOR));
    }
    throw new IllegalStateException("Unhandled case: " + version);
  }

  static SpanProcessorMetrics createForSimpleProcessor(
      InternalTelemetryVersion version, Supplier<MeterProvider> meterProvider) {
    switch (version) {
      case LEGACY:
        return SpanProcessorMetrics.noop(); // no legacy metrics for simple span processor
      case LATEST:
        return new SemConvSpanProcessorMetrics(
            meterProvider,
            ComponentId.generateLazy(StandardComponentId.SpanProcessorType.SIMPLE_SPAN_PROCESSOR));
    }
    throw new IllegalStateException("Unhandled case: " + version);
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
