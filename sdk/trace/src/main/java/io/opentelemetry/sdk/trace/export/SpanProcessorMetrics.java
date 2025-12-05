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
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import javax.annotation.Nullable;

/** Metrics exported by span processors. */
interface SpanProcessorMetrics {

  static SpanProcessorMetrics get(
      InternalTelemetryVersion telemetryVersion,
      ComponentId componentId,
      MeterProvider meterProvider) {
    switch (telemetryVersion) {
      case LEGACY:
        return new LegacyProcessorMetrics(meterProvider);
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

  final class SemConvSpanProcessorMetrics implements SpanProcessorMetrics {

    private final Meter meter;
    private final Attributes standardAttrs;
    private final Attributes droppedAttrs;

    private final LongCounter processedSpans;

    SemConvSpanProcessorMetrics(ComponentId componentId, MeterProvider meterProvider) {
      meter = meterProvider.get("io.opentelemetry.sdk.trace");

      standardAttrs =
          Attributes.of(
              SemConvAttributes.OTEL_COMPONENT_TYPE,
              componentId.getTypeName(),
              SemConvAttributes.OTEL_COMPONENT_NAME,
              componentId.getComponentName());
      droppedAttrs =
          Attributes.of(
              SemConvAttributes.OTEL_COMPONENT_TYPE,
              componentId.getTypeName(),
              SemConvAttributes.OTEL_COMPONENT_NAME,
              componentId.getComponentName(),
              SemConvAttributes.ERROR_TYPE,
              "queue_full");

      processedSpans =
          meter
              .counterBuilder("otel.sdk.processor.span.processed")
              .setUnit("span")
              .setDescription(
                  "The number of spans for which the processing has finished, either successful or failed.")
              .build();
    }

    @Override
    public void dropSpans(int count) {
      processedSpans.add(count, droppedAttrs);
    }

    /** Record metrics for spans processed, possibly with an error. */
    @Override
    public void finishSpans(int count, @Nullable String error) {
      if (error == null) {
        processedSpans.add(count, standardAttrs);
        return;
      }

      Attributes attributes =
          standardAttrs.toBuilder().put(SemConvAttributes.ERROR_TYPE, error).build();
      processedSpans.add(count, attributes);
    }

    /** Registers a metric for processor queue capacity. */
    @Override
    public void buildQueueCapacityMetric(long capacity) {
      meter
          .upDownCounterBuilder("otel.sdk.processor.span.queue.capacity")
          .setUnit("span")
          .setDescription(
              "The maximum number of spans the queue of a given instance of an SDK span processor can hold. ")
          .buildWithCallback(m -> m.record(capacity, standardAttrs));
    }

    /** Registers a metric for processor queue size. */
    @Override
    public void buildQueueSizeMetric(LongCallable getSize) {
      meter
          .upDownCounterBuilder("otel.sdk.processor.span.queue.size")
          .setUnit("span")
          .setDescription(
              "The number of spans in the queue of a given instance of an SDK span processor.")
          .buildWithCallback(m -> m.record(getSize.get(), standardAttrs));
    }
  }

  final class LegacyProcessorMetrics implements SpanProcessorMetrics {
    private static final AttributeKey<String> SPAN_PROCESSOR_TYPE_LABEL =
        AttributeKey.stringKey("processorType");
    private static final AttributeKey<Boolean> SPAN_PROCESSOR_DROPPED_LABEL =
        AttributeKey.booleanKey("dropped");
    // Legacy metrics are only created for batch span processor.
    private static final String SPAN_PROCESSOR_TYPE_VALUE =
        BatchSpanProcessor.class.getSimpleName();

    private final Meter meter;
    private final Attributes standardAttrs;
    private final Attributes droppedAttrs;

    private final LongCounter processedSpans;

    LegacyProcessorMetrics(MeterProvider meterProvider) {
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

    /** Records metrics for spans dropped because a queue is full. */
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

    /** Registers a metric for processor queue size. */
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
}
