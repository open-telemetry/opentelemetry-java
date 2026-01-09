/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/** Log processor metrics defined before they were standardized in semconv. */
final class LegacyLogRecordProcessorInstrumentation implements LogRecordProcessorInstrumentation {
  private static final AttributeKey<String> PROCESSOR_TYPE_LABEL =
      AttributeKey.stringKey("processorType");
  private static final AttributeKey<Boolean> PROCESSOR_DROPPED_LABEL =
      AttributeKey.booleanKey("dropped");
  // Legacy metrics are only created for batch log processor.
  private static final String PROCESSOR_TYPE_VALUE = BatchLogRecordProcessor.class.getSimpleName();

  private final Object lock = new Object();
  private final AtomicBoolean builtQueueMetrics = new AtomicBoolean(false);

  private final Supplier<MeterProvider> meterProvider;
  private final Attributes standardAttrs;
  private final Attributes droppedAttrs;

  @Nullable private Meter meter;
  @Nullable private volatile LongCounter processedLogs;

  LegacyLogRecordProcessorInstrumentation(Supplier<MeterProvider> meterProvider) {
    this.meterProvider = meterProvider;

    standardAttrs =
        Attributes.of(PROCESSOR_TYPE_LABEL, PROCESSOR_TYPE_VALUE, PROCESSOR_DROPPED_LABEL, false);
    droppedAttrs =
        Attributes.of(PROCESSOR_TYPE_LABEL, PROCESSOR_TYPE_VALUE, PROCESSOR_DROPPED_LABEL, true);
  }

  @Override
  public void dropLogs(int count) {
    processedLogs().add(count, droppedAttrs);
  }

  @Override
  public void finishLogs(int count, @Nullable String error) {
    // Legacy metrics only record when no error.
    if (error != null) {
      processedLogs().add(count, standardAttrs);
    }
  }

  @Override
  public void buildQueueMetricsOnce(long unusedCapacity, LongCallable getSize) {
    if (!builtQueueMetrics.compareAndSet(false, true)) {
      return;
    }
    meter()
        .gaugeBuilder("queueSize")
        .ofLongs()
        .setDescription("The number of items queued")
        .setUnit("1")
        .buildWithCallback(
            result ->
                result.record(
                    getSize.get(), Attributes.of(PROCESSOR_TYPE_LABEL, PROCESSOR_TYPE_VALUE)));
    // No capacity metric when legacy.
  }

  private LongCounter processedLogs() {
    LongCounter processedLogs = this.processedLogs;
    if (processedLogs == null) {
      synchronized (lock) {
        processedLogs = this.processedLogs;
        if (processedLogs == null) {
          processedLogs =
              meter()
                  .counterBuilder("processedLogs")
                  .setUnit("1")
                  .setDescription(
                      "The number of logs processed by the BatchLogRecordProcessor. "
                          + "[dropped=true if they were dropped due to high throughput]")
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
