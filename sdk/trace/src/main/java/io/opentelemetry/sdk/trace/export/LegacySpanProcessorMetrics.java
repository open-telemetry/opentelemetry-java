/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import javax.annotation.Nullable;

/** Span processor metrics defined before they were standardized in semconv. */
final class LegacySpanProcessorMetrics implements SpanProcessorMetrics {
  private static final AttributeKey<String> SPAN_PROCESSOR_TYPE_LABEL =
      AttributeKey.stringKey("processorType");
  private static final AttributeKey<Boolean> SPAN_PROCESSOR_DROPPED_LABEL =
      AttributeKey.booleanKey("dropped");
  // Legacy metrics are only created for batch span processor.
  private static final String SPAN_PROCESSOR_TYPE_VALUE = BatchSpanProcessor.class.getSimpleName();

  private final Meter meter;
  private final Attributes standardAttrs;
  private final Attributes droppedAttrs;

  private final LongCounter processedSpans;

  LegacySpanProcessorMetrics(MeterProvider meterProvider) {
    meter = meterProvider.get("io.opentelemetry.sdk.trace");

    processedSpans =
        meter
            .counterBuilder("processedSpans")
            .setUnit("1")
            .setDescription(
                "The number of spans processed by the BatchSpanProcessor. "
                    + "[dropped=true if they were dropped due to high throughput]")
            .build();

    standardAttrs =
        Attributes.of(
            SPAN_PROCESSOR_TYPE_LABEL,
            SPAN_PROCESSOR_TYPE_VALUE,
            SPAN_PROCESSOR_DROPPED_LABEL,
            false);
    droppedAttrs =
        Attributes.of(
            SPAN_PROCESSOR_TYPE_LABEL,
            SPAN_PROCESSOR_TYPE_VALUE,
            SPAN_PROCESSOR_DROPPED_LABEL,
            true);
  }

  @Override
  public void dropSpans(int count) {
    processedSpans.add(count, droppedAttrs);
  }

  @Override
  public void finishSpans(int count, @Nullable String error) {
    // Legacy metrics only record when no error.
    if (error != null) {
      processedSpans.add(count, standardAttrs);
    }
  }

  @Override
  public void buildQueueCapacityMetric(long capacity) {
    // No capacity metric when legacy.
  }

  @Override
  public void buildQueueSizeMetric(LongCallable queueSize) {
    meter
        .gaugeBuilder("queueSize")
        .ofLongs()
        .setDescription("The number of items queued")
        .setUnit("1")
        .buildWithCallback(
            result ->
                result.record(
                    queueSize.get(),
                    Attributes.of(SPAN_PROCESSOR_TYPE_LABEL, SPAN_PROCESSOR_TYPE_VALUE)));
  }
}
