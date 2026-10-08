/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.common.internal.SemConvConstants;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * SDK metrics exported for span processors as defined in the <a
 * href="https://opentelemetry.io/docs/specs/semconv/otel/sdk-metrics/#span-metrics">semantic
 * conventions</a>.
 */
final class SemConvSpanProcessorInstrumentation implements SpanProcessorInstrumentation {

  private final Object lock = new Object();
  private final AtomicBoolean builtQueueMetrics = new AtomicBoolean(false);

  private final Supplier<MeterProvider> meterProvider;
  private final Attributes standardAttrs;
  private final Attributes queueFullAttrs;
  private final Attributes shutdownAttrs;

  @Nullable private Meter meter;
  @Nullable private volatile LongCounter processedSpans;

  SemConvSpanProcessorInstrumentation(
      ComponentId componentId, Supplier<MeterProvider> meterProvider) {
    this.meterProvider = meterProvider;

    standardAttrs =
        Attributes.of(
            SemConvConstants.OTEL_COMPONENT_TYPE,
            componentId.getTypeName(),
            SemConvConstants.OTEL_COMPONENT_NAME,
            componentId.getComponentName());
    queueFullAttrs =
        Attributes.of(
            SemConvConstants.OTEL_COMPONENT_TYPE,
            componentId.getTypeName(),
            SemConvConstants.OTEL_COMPONENT_NAME,
            componentId.getComponentName(),
            SemConvConstants.ERROR_TYPE,
            "queue_full");
    shutdownAttrs =
        Attributes.of(
            SemConvConstants.OTEL_COMPONENT_TYPE,
            componentId.getTypeName(),
            SemConvConstants.OTEL_COMPONENT_NAME,
            componentId.getComponentName(),
            SemConvConstants.ERROR_TYPE,
            "already_shutdown");
  }

  @Override
  public void dropSpansQueueFull(int count) {
    processedSpans().add(count, queueFullAttrs);
  }

  @Override
  public void dropSpansAlreadyShutdown(int count) {
    processedSpans().add(count, shutdownAttrs);
  }

  @Override
  public void finishSpans(int count) {
    processedSpans().add(count, standardAttrs);
  }

  @Override
  public void buildQueueMetricsOnce(long capacity, LongCallable getSize) {
    if (!builtQueueMetrics.compareAndSet(false, true)) {
      return;
    }
    meter()
        .upDownCounterBuilder(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_NAME)
        .setUnit(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_UNIT)
        .setDescription(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_DESCRIPTION)
        .buildWithCallback(m -> m.record(capacity, standardAttrs));
    meter()
        .upDownCounterBuilder(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_NAME)
        .setUnit(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_UNIT)
        .setDescription(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_DESCRIPTION)
        .buildWithCallback(m -> m.record(getSize.get(), standardAttrs));
  }

  private LongCounter processedSpans() {
    LongCounter processedSpans = this.processedSpans;
    if (processedSpans == null) {
      synchronized (lock) {
        processedSpans = this.processedSpans;
        if (processedSpans == null) {
          processedSpans =
              meter()
                  .counterBuilder(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_PROCESSED_NAME)
                  .setUnit(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_PROCESSED_UNIT)
                  .setDescription(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_PROCESSED_DESCRIPTION)
                  .build();
          this.processedSpans = processedSpans;
        }
      }
    }
    return processedSpans;
  }

  private Meter meter() {
    if (meter == null) {
      // Safe to call from multiple threads.
      meter = meterProvider.get().get("io.opentelemetry.sdk.trace");
    }
    return meter;
  }
}
