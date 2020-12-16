/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import java.util.Map;
import java.util.Objects;

/** Builder class for {@link BatchSpanProcessor}. */
public final class BatchSpanProcessorBuilder extends ConfigBuilder<BatchSpanProcessorBuilder> {

  private static final String KEY_SCHEDULE_DELAY_MILLIS = "otel.bsp.schedule.delay.millis";
  private static final String KEY_MAX_QUEUE_SIZE = "otel.bsp.max.queue.size";
  private static final String KEY_MAX_EXPORT_BATCH_SIZE = "otel.bsp.max.export.batch.size";
  private static final String KEY_EXPORT_TIMEOUT_MILLIS = "otel.bsp.export.timeout.millis";
  private static final String KEY_SAMPLED = "otel.bsp.export.sampled";

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
  private long scheduleDelayMillis = DEFAULT_SCHEDULE_DELAY_MILLIS;
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private int maxExportBatchSize = DEFAULT_MAX_EXPORT_BATCH_SIZE;
  private int exporterTimeoutMillis = DEFAULT_EXPORT_TIMEOUT_MILLIS;
  private boolean exportOnlySampled = DEFAULT_EXPORT_ONLY_SAMPLED;

  BatchSpanProcessorBuilder(SpanExporter spanExporter) {
    this.spanExporter = Objects.requireNonNull(spanExporter, "spanExporter");
  }

  /**
   * Sets the configuration values from the given configuration map for only the available keys.
   *
   * @param configMap {@link Map} holding the configuration values.
   * @return this.
   */
  @Override
  protected BatchSpanProcessorBuilder fromConfigMap(
      Map<String, String> configMap, NamingConvention namingConvention) {
    configMap = namingConvention.normalize(configMap);
    Long longValue = getLongProperty(KEY_SCHEDULE_DELAY_MILLIS, configMap);
    if (longValue != null) {
      this.setScheduleDelayMillis(longValue);
    }
    Integer intValue = getIntProperty(KEY_MAX_QUEUE_SIZE, configMap);
    if (intValue != null) {
      this.setMaxQueueSize(intValue);
    }
    intValue = getIntProperty(KEY_MAX_EXPORT_BATCH_SIZE, configMap);
    if (intValue != null) {
      this.setMaxExportBatchSize(intValue);
    }
    intValue = getIntProperty(KEY_EXPORT_TIMEOUT_MILLIS, configMap);
    if (intValue != null) {
      this.setExporterTimeoutMillis(intValue);
    }
    Boolean boolValue = getBooleanProperty(KEY_SAMPLED, configMap);
    if (boolValue != null) {
      this.setExportOnlySampled(boolValue);
    }
    return this;
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
   */
  public BatchSpanProcessorBuilder setExportOnlySampled(boolean exportOnlySampled) {
    this.exportOnlySampled = exportOnlySampled;
    return this;
  }

  // Visible for testing
  boolean getExportOnlySampled() {
    return exportOnlySampled;
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
   */
  public BatchSpanProcessorBuilder setScheduleDelayMillis(long scheduleDelayMillis) {
    this.scheduleDelayMillis = scheduleDelayMillis;
    return this;
  }

  // Visible for testing
  long getScheduleDelayMillis() {
    return scheduleDelayMillis;
  }

  /**
   * Sets the maximum time an exporter will be allowed to run before being cancelled.
   *
   * <p>Default value is {@code 30000}ms
   *
   * @param exporterTimeoutMillis the timeout for exports in milliseconds.
   * @return this
   * @see BatchSpanProcessorBuilder#DEFAULT_EXPORT_TIMEOUT_MILLIS
   */
  public BatchSpanProcessorBuilder setExporterTimeoutMillis(int exporterTimeoutMillis) {
    this.exporterTimeoutMillis = exporterTimeoutMillis;
    return this;
  }

  // Visible for testing
  int getExporterTimeoutMillis() {
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
    Utils.checkArgument(maxExportBatchSize > 0, "maxExportBatchSize must be positive.");
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
        scheduleDelayMillis,
        maxQueueSize,
        maxExportBatchSize,
        exporterTimeoutMillis);
  }
}
