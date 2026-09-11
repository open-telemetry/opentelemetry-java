/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.common.internal.SemConvAttributes;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * SDK metrics exported for log processors as defined in the <a
 * href="https://opentelemetry.io/docs/specs/semconv/otel/sdk-metrics/#log-metrics">semantic
 * conventions</a>.
 */
final class SemConvLogRecordProcessorInstrumentation implements LogRecordProcessorInstrumentation {

  private final Object lock = new Object();
  private final AtomicBoolean builtQueueMetrics = new AtomicBoolean(false);

  private final Supplier<MeterProvider> meterProvider;
  private final Attributes standardAttrs;
  private final Attributes queueFullAttrs;
  private final Attributes shutdownAttrs;

  @Nullable private Meter meter;
  @Nullable private volatile LongCounter processedLogs;

  SemConvLogRecordProcessorInstrumentation(
      ComponentId componentId, Supplier<MeterProvider> meterProvider) {
    this.meterProvider = meterProvider;

    standardAttrs =
        Attributes.of(
            SemConvAttributes.OTEL_COMPONENT_TYPE,
            componentId.getTypeName(),
            SemConvAttributes.OTEL_COMPONENT_NAME,
            componentId.getComponentName());
    queueFullAttrs =
        Attributes.of(
            SemConvAttributes.OTEL_COMPONENT_TYPE,
            componentId.getTypeName(),
            SemConvAttributes.OTEL_COMPONENT_NAME,
            componentId.getComponentName(),
            SemConvAttributes.ERROR_TYPE,
            "queue_full");
    shutdownAttrs =
        Attributes.of(
            SemConvAttributes.OTEL_COMPONENT_TYPE,
            componentId.getTypeName(),
            SemConvAttributes.OTEL_COMPONENT_NAME,
            componentId.getComponentName(),
            SemConvAttributes.ERROR_TYPE,
            "already_shutdown");
  }

  @Override
  public void dropLogsQueueFull(int count) {
    processedLogs().add(count, queueFullAttrs);
  }

  @Override
  public void dropLogsAlreadyShutdown(int count) {
    processedLogs().add(count, shutdownAttrs);
  }

  @Override
  public void finishLogs(int count) {
    processedLogs().add(count, standardAttrs);
  }

  @Override
  public void buildQueueMetricsOnce(long capacity, LongCallable getSize) {
    if (!builtQueueMetrics.compareAndSet(false, true)) {
      return;
    }
    meter()
        .upDownCounterBuilder("otel.sdk.processor.log.queue.capacity")
        .setUnit("{log_record}")
        .setDescription(
            "The maximum number of log records the queue of a given instance of an SDK Log Record processor can hold. ")
        .buildWithCallback(m -> m.record(capacity, standardAttrs));
    meter()
        .upDownCounterBuilder("otel.sdk.processor.log.queue.size")
        .setUnit("{log_record}")
        .setDescription(
            "The number of log records in the queue of a given instance of an SDK log processor.")
        .buildWithCallback(m -> m.record(getSize.get(), standardAttrs));
  }

  private LongCounter processedLogs() {
    LongCounter processedLogs = this.processedLogs;
    if (processedLogs == null) {
      synchronized (lock) {
        processedLogs = this.processedLogs;
        if (processedLogs == null) {
          processedLogs =
              meter()
                  .counterBuilder("otel.sdk.processor.log.processed")
                  .setUnit("{log_record}")
                  .setDescription(
                      "The number of log records for which the processing has finished, either successful or failed.")
                  .build();
          this.processedLogs = processedLogs;
        }
      }
    }
    return processedLogs;
  }

  private Meter meter() {
    if (meter == null) {
      // Safe to call from multiple threads.
      meter = meterProvider.get().get("io.opentelemetry.sdk.logs");
    }
    return meter;
  }
}
