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
import io.opentelemetry.sdk.common.internal.SemConvConstants;
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
        .upDownCounterBuilder(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_NAME)
        .setUnit(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_UNIT)
        .setDescription(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_DESCRIPTION)
        .buildWithCallback(m -> m.record(capacity, standardAttrs));
    meter()
        .upDownCounterBuilder(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_NAME)
        .setUnit(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_UNIT)
        .setDescription(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_DESCRIPTION)
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
                  .counterBuilder(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_PROCESSED_NAME)
                  .setUnit(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_PROCESSED_UNIT)
                  .setDescription(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_PROCESSED_DESCRIPTION)
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
