/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Builder class for {@link BatchSpanProcessor}. */
public final class BatchSpanProcessorBuilder {

  // Visible for testing
  static final long DEFAULT_SCHEDULE_DELAY_MILLIS = 5000;
  // Visible for testing
  static final int DEFAULT_MAX_QUEUE_SIZE = 2048;
  // Visible for testing
  static final int DEFAULT_MAX_EXPORT_BATCH_SIZE = 512;
  // Visible for testing
  static final int DEFAULT_EXPORT_TIMEOUT_MILLIS = 30_000;
  // Visible for testing
  static final boolean DEFAULT_EXPORT_ONLY_SAMPLED = true;

  private final SpanExporter spanExporter;
  private long scheduleDelayNanos = TimeUnit.MILLISECONDS.toNanos(DEFAULT_SCHEDULE_DELAY_MILLIS);
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private int maxExportBatchSize = DEFAULT_MAX_EXPORT_BATCH_SIZE;
  private long exporterTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(DEFAULT_EXPORT_TIMEOUT_MILLIS);
  private boolean exportOnlySampled = DEFAULT_EXPORT_ONLY_SAMPLED;

  BatchSpanProcessorBuilder(SpanExporter spanExporter) {
    this.spanExporter = requireNonNull(spanExporter, "spanExporter");
  }

  // TODO: Consider to add support for constant Attributes and/or Resource.

  /**
   * Set whether only sampled spans should be reported.
   *
   * <p>Default value is {@code true}.
   *
   * @param exportOnlySampled if {@code true} report only sampled spans.
   * @return this.
   * @see BatchSpanProcessorBuilder#DEFAULT_EXPORT_ONLY_SAMPLED
   * @deprecated Will be removed without replacement, all spans with a sampling result of {@link
   *     io.opentelemetry.sdk.trace.samplers.SamplingResult.Decision#RECORD_AND_SAMPLE} will be
   *     exported while spans with a result of {@link
   *     io.opentelemetry.sdk.trace.samplers.SamplingResult.Decision#RECORD_ONLY} will not.
   */
  @Deprecated
  public BatchSpanProcessorBuilder setExportOnlySampled(boolean exportOnlySampled) {
    this.exportOnlySampled = exportOnlySampled;
    return this;
  }

  // Visible for testing
  boolean getExportOnlySampled() {
    return exportOnlySampled;
  }

  /**
   * Sets the delay interval between two consecutive exports. If unset, defaults to {@value
   * DEFAULT_SCHEDULE_DELAY_MILLIS}ms.
   */
  public BatchSpanProcessorBuilder setScheduleDelay(long delay, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(delay >= 0, "delay must be non-negative");
    scheduleDelayNanos = unit.toNanos(delay);
    return this;
  }

  /**
   * Sets the delay interval between two consecutive exports. If unset, defaults to {@value
   * DEFAULT_SCHEDULE_DELAY_MILLIS}ms.
   */
  public BatchSpanProcessorBuilder setScheduleDelay(Duration delay) {
    requireNonNull(delay, "delay");
    return setScheduleDelay(delay.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the delay interval between two consecutive exports. The actual interval may be shorter if
   * the batch size is getting larger than {@code maxQueuedSpans / 2}.
   *
   * <p>Default value is {@code 5000}ms.
   *
   * @param scheduleDelayMillis the delay interval between two consecutive exports.
   * @return this.
   * @see BatchSpanProcessorBuilder#DEFAULT_SCHEDULE_DELAY_MILLIS
   * @deprecated Use {@link #setScheduleDelay(long, TimeUnit)}
   */
  @Deprecated
  public BatchSpanProcessorBuilder setScheduleDelayMillis(long scheduleDelayMillis) {
    return setScheduleDelay(Duration.ofMillis(scheduleDelayMillis));
  }

  // Visible for testing
  long getScheduleDelayNanos() {
    return scheduleDelayNanos;
  }

  /**
   * Sets the maximum time an export will be allowed to run before being cancelled. If unset,
   * defaults to {@value DEFAULT_EXPORT_TIMEOUT_MILLIS}ms.
   */
  public BatchSpanProcessorBuilder setExporterTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    exporterTimeoutNanos = unit.toNanos(timeout);
    return this;
  }

  /**
   * Sets the maximum time an export will be allowed to run before being cancelled. If unset,
   * defaults to {@value DEFAULT_EXPORT_TIMEOUT_MILLIS}ms.
   */
  public BatchSpanProcessorBuilder setExporterTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    return setExporterTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the maximum time an exporter will be allowed to run before being cancelled.
   *
   * <p>Default value is {@code 30000}ms
   *
   * @param exporterTimeoutMillis the timeout for exports in milliseconds.
   * @return this
   * @see BatchSpanProcessorBuilder#DEFAULT_EXPORT_TIMEOUT_MILLIS
   * @deprecated Use {@link #setExporterTimeout(long, TimeUnit)}
   */
  @Deprecated
  public BatchSpanProcessorBuilder setExporterTimeoutMillis(int exporterTimeoutMillis) {
    return setExporterTimeout(Duration.ofMillis(exporterTimeoutMillis));
  }

  // Visible for testing
  long getExporterTimeoutNanos() {
    return exporterTimeoutNanos;
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
   * @see BatchSpanProcessorBuilder#DEFAULT_MAX_QUEUE_SIZE
   */
  public BatchSpanProcessorBuilder setMaxQueueSize(int maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
    return this;
  }

  // Visible for testing
  int getMaxQueueSize() {
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
   * @see BatchSpanProcessorBuilder#DEFAULT_MAX_EXPORT_BATCH_SIZE
   */
  public BatchSpanProcessorBuilder setMaxExportBatchSize(int maxExportBatchSize) {
    checkArgument(maxExportBatchSize > 0, "maxExportBatchSize must be positive.");
    this.maxExportBatchSize = maxExportBatchSize;
    return this;
  }

  // Visible for testing
  int getMaxExportBatchSize() {
    return maxExportBatchSize;
  }

  /**
   * Returns a new {@link BatchSpanProcessor} that batches, then converts spans to proto and
   * forwards them to the given {@code spanExporter}.
   *
   * @return a new {@link BatchSpanProcessor}.
   * @throws NullPointerException if the {@code spanExporter} is {@code null}.
   */
  public BatchSpanProcessor build() {
    return new BatchSpanProcessor(
        spanExporter,
        exportOnlySampled,
        scheduleDelayNanos,
        maxQueueSize,
        maxExportBatchSize,
        exporterTimeoutNanos);
  }
}
