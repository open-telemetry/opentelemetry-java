/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import com.google.auto.value.AutoValue;

/**
 * Settings for a {@link io.opentelemetry.sdk.trace.SpanProcessor} that batches spans exported by
 * the SDK then pushes them to the exporter pipeline.
 *
 * <p>All spans reported by the SDK implementation are first added to a synchronized queue (with a
 * {@code maxQueueSize} maximum size, if queue is full spans are dropped). Spans are exported either
 * when there are {@code maxExportBatchSize} pending spans or {@code scheduleDelayMillis} has passed
 * since the last export finished.
 */
@AutoValue
public abstract class BatchSettings {

  // Marker instance compared by reference when setting up exporters without batching.
  private static final BatchSettings NO_BATCHING = new AutoValue_BatchSettings(-1, -1, -1, -1);

  /** Returns a {@link BatchSettings} indicating batching is disabled. */
  public static BatchSettings noBatching() {
    return NO_BATCHING;
  }

  /** Returns a builder of {@link BatchSettings}. */
  public static BatchSettingsBuilder builder() {
    return new BatchSettingsBuilder();
  }

  static BatchSettings create(
      long scheduleDelayNanos,
      long exporterTimeoutNanos,
      int maxQueueSize,
      int maxExportBatchSize) {
    return new AutoValue_BatchSettings(
        scheduleDelayNanos, exporterTimeoutNanos, maxQueueSize, maxExportBatchSize);
  }

  /**
   * The delay interval between two consecutive exports. The actual interval may be shorter if the
   * batch size is getting larger than {@code maxQueuedSpans / 2}. * @return
   */
  public abstract long getScheduleDelayNanos();

  /** The maximum time an exporter will be allowed to run before being cancelled. */
  public abstract long getExporterTimeoutNanos();

  /** The maximum number of Spans that are kept in the queue before start dropping. */
  public abstract int getMaxQueueSize();

  /** The maximum batch size for every export. */
  public abstract int getMaxExportBatchSize();

  BatchSettings() {}
}
