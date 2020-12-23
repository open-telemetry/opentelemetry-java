/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static io.opentelemetry.api.internal.Utils.checkArgument;

import java.util.concurrent.TimeUnit;

/** Builder for a {@link BatchSettings}. */
public final class BatchSettingsBuilder {

  // Visible for testing
  static final long DEFAULT_SCHEDULE_DELAY_MILLIS = 5000;
  // Visible for testing
  static final int DEFAULT_MAX_QUEUE_SIZE = 2048;
  // Visible for testing
  static final int DEFAULT_MAX_EXPORT_BATCH_SIZE = 512;
  // Visible for testing
  static final int DEFAULT_EXPORT_TIMEOUT_MILLIS = 30_000;
  // Visible for testing

  private long scheduleDelayMillis = DEFAULT_SCHEDULE_DELAY_MILLIS;
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private int maxExportBatchSize = DEFAULT_MAX_EXPORT_BATCH_SIZE;
  private int exporterTimeoutMillis = DEFAULT_EXPORT_TIMEOUT_MILLIS;

  /**
   * Sets the delay interval between two consecutive exports. The actual interval may be shorter if
   * the batch size is getting larger than {@code maxQueuedSpans / 2}.
   *
   * <p>Default value is {@value DEFAULT_SCHEDULE_DELAY_MILLIS}ms.
   */
  public BatchSettingsBuilder setScheduleDelay(long scheduleDelay, TimeUnit unit) {
    checkArgument(scheduleDelay > 0, "scheduleDelay must be positive");
    this.scheduleDelayMillis = unit.toMillis(scheduleDelay);
    return this;
  }

  /**
   * Sets the maximum time an exporter will be allowed to run before being cancelled.
   *
   * <p>Default value is {@value DEFAULT_EXPORT_TIMEOUT_MILLIS}ms
   */
  public BatchSettingsBuilder setExporterTimeout(int exporterTimeout, TimeUnit unit) {
    checkArgument(exporterTimeout > 0, "exporterTimeout must be positive");
    this.exporterTimeoutMillis = (int) unit.toMillis(exporterTimeout);
    return this;
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
        scheduleDelayMillis, exporterTimeoutMillis, maxQueueSize, maxExportBatchSize);
  }

  BatchSettingsBuilder() {}
}
