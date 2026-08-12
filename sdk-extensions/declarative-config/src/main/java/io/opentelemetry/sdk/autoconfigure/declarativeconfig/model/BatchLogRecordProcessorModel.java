/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "schedule_delay",
  "export_timeout",
  "max_queue_size",
  "max_export_batch_size",
  "exporter"
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class BatchLogRecordProcessorModel {

  @Nullable private Integer scheduleDelay;
  @Nullable private Integer exportTimeout;
  @Nullable private Integer maxQueueSize;
  @Nullable private Integer maxExportBatchSize;
  @Nullable private LogRecordExporterModel exporter;

  /**
   * Configure delay interval (in milliseconds) between two consecutive exports.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, 1000 is used.
   */
  @JsonProperty("schedule_delay")
  @Nullable
  public Integer getScheduleDelay() {
    return scheduleDelay;
  }

  @JsonProperty("schedule_delay")
  public BatchLogRecordProcessorModel withScheduleDelay(Integer scheduleDelay) {
    this.scheduleDelay = scheduleDelay;
    return this;
  }

  /**
   * Configure maximum allowed time (in milliseconds) to export data.
   *
   * <p>Value must be non-negative. A value of 0 indicates no limit (infinity).
   *
   * <p>If omitted or null, 30000 is used.
   */
  @JsonProperty("export_timeout")
  @Nullable
  public Integer getExportTimeout() {
    return exportTimeout;
  }

  @JsonProperty("export_timeout")
  public BatchLogRecordProcessorModel withExportTimeout(Integer exportTimeout) {
    this.exportTimeout = exportTimeout;
    return this;
  }

  /**
   * Configure maximum queue size. Value must be positive.
   *
   * <p>If omitted or null, 2048 is used.
   */
  @JsonProperty("max_queue_size")
  @Nullable
  public Integer getMaxQueueSize() {
    return maxQueueSize;
  }

  @JsonProperty("max_queue_size")
  public BatchLogRecordProcessorModel withMaxQueueSize(Integer maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
    return this;
  }

  /**
   * Configure maximum batch size. Value must be positive.
   *
   * <p>If omitted or null, 512 is used.
   */
  @JsonProperty("max_export_batch_size")
  @Nullable
  public Integer getMaxExportBatchSize() {
    return maxExportBatchSize;
  }

  @JsonProperty("max_export_batch_size")
  public BatchLogRecordProcessorModel withMaxExportBatchSize(Integer maxExportBatchSize) {
    this.maxExportBatchSize = maxExportBatchSize;
    return this;
  }

  /**
   * Configure exporter.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("exporter")
  @Nullable
  public LogRecordExporterModel getExporter() {
    return exporter;
  }

  @JsonProperty("exporter")
  public BatchLogRecordProcessorModel withExporter(LogRecordExporterModel exporter) {
    this.exporter = exporter;
    return this;
  }

  @Override
  public String toString() {
    return "BatchLogRecordProcessorModel{"
        + "scheduleDelay="
        + scheduleDelay
        + ", exportTimeout="
        + exportTimeout
        + ", maxQueueSize="
        + maxQueueSize
        + ", maxExportBatchSize="
        + maxExportBatchSize
        + ", exporter="
        + exporter
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.scheduleDelay == null) ? 0 : this.scheduleDelay.hashCode();
    h *= 1000003;
    h ^= (this.exportTimeout == null) ? 0 : this.exportTimeout.hashCode();
    h *= 1000003;
    h ^= (this.maxQueueSize == null) ? 0 : this.maxQueueSize.hashCode();
    h *= 1000003;
    h ^= (this.maxExportBatchSize == null) ? 0 : this.maxExportBatchSize.hashCode();
    h *= 1000003;
    h ^= (this.exporter == null) ? 0 : this.exporter.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof BatchLogRecordProcessorModel) {
      BatchLogRecordProcessorModel that = (BatchLogRecordProcessorModel) o;
      return (this.scheduleDelay == null
              ? that.scheduleDelay == null
              : this.scheduleDelay.equals(that.scheduleDelay))
          && (this.exportTimeout == null
              ? that.exportTimeout == null
              : this.exportTimeout.equals(that.exportTimeout))
          && (this.maxQueueSize == null
              ? that.maxQueueSize == null
              : this.maxQueueSize.equals(that.maxQueueSize))
          && (this.maxExportBatchSize == null
              ? that.maxExportBatchSize == null
              : this.maxExportBatchSize.equals(that.maxExportBatchSize))
          && (this.exporter == null ? that.exporter == null : this.exporter.equals(that.exporter));
    }
    return false;
  }
}
