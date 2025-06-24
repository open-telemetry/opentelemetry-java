/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;

class LegacyBatchSpanProcessorMetrics implements SpanProcessorMetrics {

  public static final AttributeKey<String> SPAN_PROCESSOR_TYPE_LABEL =
      AttributeKey.stringKey("processorType");
  public static final AttributeKey<Boolean> SPAN_PROCESSOR_DROPPED_LABEL =
      AttributeKey.booleanKey("dropped");
  public static final String SPAN_PROCESSOR_TYPE_VALUE = BatchSpanProcessor.class.getSimpleName();

  private static final Attributes DROPPED_ATTRIBS =
      Attributes.of(
          LegacyBatchSpanProcessorMetrics.SPAN_PROCESSOR_TYPE_LABEL,
          LegacyBatchSpanProcessorMetrics.SPAN_PROCESSOR_TYPE_VALUE,
          LegacyBatchSpanProcessorMetrics.SPAN_PROCESSOR_DROPPED_LABEL,
          true);

  private static final Attributes EXPORTED_ATTRIBS =
      Attributes.of(
          LegacyBatchSpanProcessorMetrics.SPAN_PROCESSOR_TYPE_LABEL,
          LegacyBatchSpanProcessorMetrics.SPAN_PROCESSOR_TYPE_VALUE,
          LegacyBatchSpanProcessorMetrics.SPAN_PROCESSOR_DROPPED_LABEL,
          false);

  private final Supplier<MeterProvider> meterProviderSupplier;

  @Nullable private volatile LongCounter processedSpans;

  private volatile boolean queueMetricInitialized = false;

  @Nullable private ObservableLongGauge queueSize;

  LegacyBatchSpanProcessorMetrics(Supplier<MeterProvider> meterProviderSupplier) {
    this.meterProviderSupplier = meterProviderSupplier;
  }

  private Meter meter() {
    MeterProvider meterProvider = meterProviderSupplier.get();
    if (meterProvider == null) {
      meterProvider = MeterProvider.noop();
    }
    return meterProvider.get("io.opentelemetry.sdk.trace");
  }

  private LongCounter processedSpans() {
    LongCounter processedSpans = this.processedSpans;
    if (processedSpans == null) {
      processedSpans =
          meter()
              .counterBuilder("processedSpans")
              .setUnit("1")
              .setDescription(
                  "The number of spans processed by the BatchSpanProcessor. "
                      + "[dropped=true if they were dropped due to high throughput]")
              .build();
      this.processedSpans = processedSpans;
    }
    return processedSpans;
  }

  @Override
  public void recordSpansProcessed(long count, @Nullable String errorType) {
    // Only used by legacy metrics for dropped spans
    if (errorType != null) {
      processedSpans().add(count, DROPPED_ATTRIBS);
    }
  }

  @Override
  public void recordSpansExportedSuccessfully(long count) {
    processedSpans().add(count, EXPORTED_ATTRIBS);
  }

  @Override
  public void startRecordingQueueMetrics(
      LongSupplier queueSizeSupplier, LongSupplier queueCapacitySupplier) {
    if (queueMetricInitialized) {
      return;
    }
    synchronized (this) {
      if (queueMetricInitialized) {
        return;
      }
      this.queueSize =
          meter()
              .gaugeBuilder("queueSize")
              .ofLongs()
              .setDescription("The number of items queued")
              .setUnit("1")
              .buildWithCallback(
                  result ->
                      result.record(
                          queueSizeSupplier.getAsLong(),
                          Attributes.of(
                              LegacyBatchSpanProcessorMetrics.SPAN_PROCESSOR_TYPE_LABEL,
                              LegacyBatchSpanProcessorMetrics.SPAN_PROCESSOR_TYPE_VALUE)));
    }
  }

  @Override
  public synchronized void close() {
    queueMetricInitialized = true; // to prevent initialization after close
    if (queueSize != null) {
      queueSize.close();
    }
  }
}
