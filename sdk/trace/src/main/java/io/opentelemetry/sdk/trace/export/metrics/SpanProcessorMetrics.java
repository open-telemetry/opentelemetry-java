/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export.metrics;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.StandardComponentId;
import io.opentelemetry.sdk.trace.SpanProcessor;

import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface SpanProcessorMetrics extends AutoCloseable {

  /**
   * This value is defined in the semantic conventions.
   */
  String QUEUE_FULL_DROPPED_ERROR_TYPE = "queue_full";

  static SpanProcessorMetrics noop() {
    return NoopSpanProcessorMetrics.INSTANCE;
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

  /** Can be called multiple times and concurrently, but only the first invocation will have an effect. */
  void startRecordingQueueMetrics(
      LongSupplier queueSizeSupplier, LongSupplier queueCapacitySupplier);

  /**
   * Must be called if {@link #startRecordingQueueMetrics(LongSupplier, LongSupplier)} was called,
   * otherwise not required.
   */
  @Override
  void close();
}
