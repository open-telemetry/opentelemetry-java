/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import javax.annotation.Nullable;

/**
 * SDK metrics exported for span processors as defined in the <a
 * href="https://opentelemetry.io/docs/specs/semconv/otel/sdk-metrics/#span-metrics">semantic
 * conventions</a>.
 */
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
