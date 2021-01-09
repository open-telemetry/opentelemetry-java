/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Builder for a {@link BatchSettings}. */
public final class BatchSettingsBuilder {

  // Visible for testing
  static final long DEFAULT_SCHEDULE_DELAY_SECS = 5;
  // Visible for testing
  static final int DEFAULT_MAX_QUEUE_SIZE = 2048;
  // Visible for testing
  static final int DEFAULT_MAX_EXPORT_BATCH_SIZE = 512;
  // Visible for testing
  static final int DEFAULT_EXPORT_TIMEOUT_SECS = 30;
  // Visible for testing

  private long scheduleDelayNanos = TimeUnit.SECONDS.toNanos(DEFAULT_SCHEDULE_DELAY_SECS);
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private int maxExportBatchSize = DEFAULT_MAX_EXPORT_BATCH_SIZE;
  private long exporterTimeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_EXPORT_TIMEOUT_SECS);

  /**
   * Sets the delay interval between two consecutive exports. If unset, defaults to {@value
   * DEFAULT_SCHEDULE_DELAY_SECS}s.
   */
  public BatchSettingsBuilder setScheduleDelay(long delay, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(delay >= 0, "delay must be non-negative");
    scheduleDelayNanos = unit.toNanos(delay);
    return this;
  }

  /**
   * Sets the delay interval between two consecutive exports. If unset, defaults to {@value
   * DEFAULT_SCHEDULE_DELAY_SECS}s.
   */
  public BatchSettingsBuilder setScheduleDelay(Duration delay) {
    requireNonNull(delay, "delay");
    return setScheduleDelay(delay.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the maximum time an export will be allowed to run before being cancelled. If unset,
   * defaults to {@value DEFAULT_EXPORT_TIMEOUT_SECS}s.
   */
  public BatchSettingsBuilder setExporterTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    exporterTimeoutNanos = unit.toNanos(timeout);
    return this;
  }

  /**
   * Sets the maximum time an export will be allowed to run before being cancelled. If unset,
   * defaults to {@value DEFAULT_EXPORT_TIMEOUT_SECS}s.
   */
  public BatchSettingsBuilder setExporterTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    return setExporterTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the maximum number of Spans that are kept in the queue before start dropping.
   *
   * <p>See the BatchSampledSpansProcessor class description for a high-level design description of
   * this class.
   *
   * <p>Default value is {@value DEFAULT_MAX_QUEUE_SIZE}.
   */
  public BatchSettingsBuilder setMaxQueueSize(int maxQueueSize) {
    checkArgument(maxExportBatchSize >= 0, "maxQueueSize must be non-negative.");
    this.maxQueueSize = maxQueueSize;
    return this;
  }

  /**
   * Sets the maximum batch size for every export. This must be smaller or equal to {@code
   * maxQueueSize}.
   *
   * <p>Default value is {@value DEFAULT_MAX_EXPORT_BATCH_SIZE}.
   */
  public BatchSettingsBuilder setMaxExportBatchSize(int maxExportBatchSize) {
    checkArgument(maxExportBatchSize > 0, "maxExportBatchSize must be positive.");
    this.maxExportBatchSize = maxExportBatchSize;
    return this;
  }

  /** Returns a {@link BatchSettings} with the settings of this {@link BatchSettingsBuilder}. */
  public BatchSettings build() {
    checkArgument(
        maxExportBatchSize <= maxQueueSize,
        "maxExportBatchSize must be less than or equal to maxQueueSize");
    return BatchSettings.create(
        scheduleDelayNanos, exporterTimeoutNanos, maxQueueSize, maxExportBatchSize);
  }

  BatchSettingsBuilder() {}
}
