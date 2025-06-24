/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.ObservableLongUpDownCounter;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;

class SemConvSpanProcessorMetrics implements SpanProcessorMetrics {

  private volatile boolean queueMetricsStarted = false;

  @Nullable private ObservableLongUpDownCounter queueCapacity;
  @Nullable private ObservableLongUpDownCounter queueSize;
  @Nullable private LongCounter processed;

  private final Supplier<MeterProvider> meterProviderSupplier;
  private final ComponentId componentId;

  @Nullable private volatile Attributes attributes = null;

  SemConvSpanProcessorMetrics(
      Supplier<MeterProvider> meterProviderSupplier, ComponentId componentId) {
    this.meterProviderSupplier = meterProviderSupplier;
    this.componentId = componentId;
  }

  private Attributes attributes() {
    // attributes are initialized lazily to trigger lazy initialization of the componentId
    Attributes attribs = this.attributes;
    if (attribs == null) {
      AttributesBuilder builder = Attributes.builder();
      builder.put(SemConvAttributes.OTEL_COMPONENT_TYPE, componentId.getTypeName());
      builder.put(SemConvAttributes.OTEL_COMPONENT_NAME, componentId.getComponentName());
      attribs = builder.build();
      this.attributes = attribs;
    }
    return attribs;
  }

  private Meter meter() {
    MeterProvider meterProvider = meterProviderSupplier.get();
    if (meterProvider == null) {
      meterProvider = MeterProvider.noop();
    }
    return meterProvider.get("io.opentelemetry.processor." + componentId.getTypeName());
  }

  private LongCounter processed() {
    LongCounter processed = this.processed;
    if (processed == null) {
      processed =
          meter()
              .counterBuilder("otel.sdk.processor.span.processed")
              .setUnit("{span}")
              .setDescription("The number of spans for which the processing has finished")
              .build();
      this.processed = processed;
    }
    return processed;
  }

  @Override
  public void startRecordingQueueMetrics(
      LongSupplier queueSizeSupplier, LongSupplier queueCapacitySupplier) {
    if (queueMetricsStarted) {
      return;
    }
    synchronized (this) {
      if (queueMetricsStarted) {
        return;
      }
      queueSize =
          meter()
              .upDownCounterBuilder("otel.sdk.processor.span.queue.size")
              .setUnit("{span}")
              .setDescription(
                  "The number of spans in the queue of a given instance of an SDK span processor")
              .buildWithCallback(
                  measurement -> {
                    measurement.record(queueSizeSupplier.getAsLong(), attributes());
                  });
      queueCapacity =
          meter()
              .upDownCounterBuilder("otel.sdk.processor.span.queue.capacity")
              .setUnit("{span}")
              .setDescription(
                  "The maximum number of spans the queue of a given instance of an SDK span processor can hold")
              .buildWithCallback(
                  measurement -> {
                    measurement.record(queueCapacitySupplier.getAsLong(), attributes());
                  });
      queueMetricsStarted = true;
    }
  }

  @Override
  public synchronized void close() {
    queueMetricsStarted = true; // prevent initialization after close
    if (queueCapacity != null) {
      queueCapacity.close();
    }
    if (queueSize != null) {
      queueSize.close();
    }
  }

  @Override
  public void recordSpansProcessed(long count, @Nullable String errorType) {
    Attributes attribs = attributes();
    if (errorType != null) {
      attribs = attribs.toBuilder().put(SemConvAttributes.ERROR_TYPE, errorType).build();
    }
    processed().add(count, attribs);
  }

  @Override
  public void recordSpansExportedSuccessfully(long count) {
    // Not used by semconv metrics
  }
}
