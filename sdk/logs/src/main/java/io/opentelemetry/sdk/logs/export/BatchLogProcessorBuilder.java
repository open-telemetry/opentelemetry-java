/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.api.metrics.MeterProvider;
import java.util.Objects;

public final class BatchLogProcessorBuilder {
  private static final long DEFAULT_SCHEDULE_DELAY_MILLIS = 200;
  private static final int DEFAULT_MAX_QUEUE_SIZE = 2048;
  private static final int DEFAULT_MAX_EXPORT_BATCH_SIZE = 512;
  private static final long DEFAULT_EXPORT_TIMEOUT_MILLIS = 30_000;

  private final LogExporter logExporter;
  private long scheduleDelayMillis = DEFAULT_SCHEDULE_DELAY_MILLIS;
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private int maxExportBatchSize = DEFAULT_MAX_EXPORT_BATCH_SIZE;
  private long exporterTimeoutMillis = DEFAULT_EXPORT_TIMEOUT_MILLIS;
  private MeterProvider meterProvider = MeterProvider.noop();

  BatchLogProcessorBuilder(LogExporter logExporter) {
    this.logExporter = Objects.requireNonNull(logExporter, "Exporter argument can not be null");
  }

  /**
   * Sets the delay interval between two consecutive exports. The actual interval may be shorter if
   * the batch size is getting larger than {@code maxQueuedSpans / 2}.
   *
   * <p>Default value is {@code 250}ms.
   *
   * @param scheduleDelayMillis the delay interval between two consecutive exports.
   * @return this.
   * @see BatchLogProcessorBuilder#DEFAULT_SCHEDULE_DELAY_MILLIS
   */
  public BatchLogProcessorBuilder setScheduleDelayMillis(long scheduleDelayMillis) {
    this.scheduleDelayMillis = scheduleDelayMillis;
    return this;
  }

  public long getScheduleDelayMillis() {
    return scheduleDelayMillis;
  }

  /**
   * Sets the maximum time an exporter will be allowed to run before being cancelled.
   *
   * <p>Default value is {@code 30000}ms
   *
   * @param exporterTimeoutMillis the timeout for exports in milliseconds.
   * @return this
   * @see BatchLogProcessorBuilder#DEFAULT_EXPORT_TIMEOUT_MILLIS
   */
  public BatchLogProcessorBuilder setExporterTimeoutMillis(int exporterTimeoutMillis) {
    this.exporterTimeoutMillis = exporterTimeoutMillis;
    return this;
  }

  public long getExporterTimeoutMillis() {
    return exporterTimeoutMillis;
  }

  /**
   * Sets the maximum number of Spans that are kept in the queue before start dropping.
   *
   * <p>See the BatchSampledSpansProcessor class description for a high-level design description of
   * this class.
   *
   * <p>Default value is {@code 2048}.
   *
   * @param maxQueueSize the maximum number of Spans that are kept in the queue before start
   *     dropping.
   * @return this.
   * @see BatchLogProcessorBuilder#DEFAULT_MAX_QUEUE_SIZE
   */
  public BatchLogProcessorBuilder setMaxQueueSize(int maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
    return this;
  }

  public int getMaxQueueSize() {
    return maxQueueSize;
  }

  /**
   * Sets the maximum batch size for every export. This must be smaller or equal to {@code
   * maxQueuedSpans}.
   *
   * <p>Default value is {@code 512}.
   *
   * @param maxExportBatchSize the maximum batch size for every export.
   * @return this.
   * @see BatchLogProcessorBuilder#DEFAULT_MAX_EXPORT_BATCH_SIZE
   */
  public BatchLogProcessorBuilder setMaxExportBatchSize(int maxExportBatchSize) {
    Utils.checkArgument(maxExportBatchSize > 0, "maxExportBatchSize must be positive.");
    this.maxExportBatchSize = maxExportBatchSize;
    return this;
  }

  /**
   * Sets the {@link MeterProvider} to use to collect metrics related to export. If not set, metrics
   * will not be collected.
   */
  public BatchLogProcessorBuilder setMeterProvider(MeterProvider meterProvider) {
    requireNonNull(meterProvider, "meterProvider");
    this.meterProvider = meterProvider;
    return this;
  }

  /**
   * Build a BatchLogProcessor.
   *
   * @return configured processor
   */
  public BatchLogProcessor build() {
    return new BatchLogProcessor(
        maxQueueSize,
        scheduleDelayMillis,
        maxExportBatchSize,
        exporterTimeoutMillis,
        logExporter,
        meterProvider);
  }
}
