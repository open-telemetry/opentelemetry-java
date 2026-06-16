/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "schedule_delay",
  "export_timeout",
  "max_queue_size",
  "max_export_batch_size",
  "exporter"
})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class BatchLogRecordProcessorModel {

  /**
   * Configure delay interval (in milliseconds) between two consecutive exports. Value must be
   * non-negative. If omitted or null, 1000 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("schedule_delay")
  @JsonPropertyDescription(
      "Configure delay interval (in milliseconds) between two consecutive exports. \nValue must be non-negative.\nIf omitted or null, 1000 is used.\n")
  private Integer scheduleDelay;

  /**
   * Configure maximum allowed time (in milliseconds) to export data. Value must be non-negative. A
   * value of 0 indicates no limit (infinity). If omitted or null, 30000 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("export_timeout")
  @JsonPropertyDescription(
      "Configure maximum allowed time (in milliseconds) to export data. \nValue must be non-negative. A value of 0 indicates no limit (infinity).\nIf omitted or null, 30000 is used.\n")
  private Integer exportTimeout;

  /**
   * Configure maximum queue size. Value must be positive. If omitted or null, 2048 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("max_queue_size")
  @JsonPropertyDescription(
      "Configure maximum queue size. Value must be positive.\nIf omitted or null, 2048 is used.\n")
  private Integer maxQueueSize;

  /**
   * Configure maximum batch size. Value must be positive. If omitted or null, 512 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("max_export_batch_size")
  @JsonPropertyDescription(
      "Configure maximum batch size. Value must be positive.\nIf omitted or null, 512 is used.\n")
  private Integer maxExportBatchSize;

  /** (Required) */
  @JsonProperty("exporter")
  @Nonnull
  private LogRecordExporterModel exporter;

  /**
   * Configure delay interval (in milliseconds) between two consecutive exports. Value must be
   * non-negative. If omitted or null, 1000 is used.
   */
  @JsonProperty("schedule_delay")
  @Nullable
  public Integer getScheduleDelay() {
    return scheduleDelay;
  }

  public BatchLogRecordProcessorModel withScheduleDelay(Integer scheduleDelay) {
    this.scheduleDelay = scheduleDelay;
    return this;
  }

  /**
   * Configure maximum allowed time (in milliseconds) to export data. Value must be non-negative. A
   * value of 0 indicates no limit (infinity). If omitted or null, 30000 is used.
   */
  @JsonProperty("export_timeout")
  @Nullable
  public Integer getExportTimeout() {
    return exportTimeout;
  }

  public BatchLogRecordProcessorModel withExportTimeout(Integer exportTimeout) {
    this.exportTimeout = exportTimeout;
    return this;
  }

  /** Configure maximum queue size. Value must be positive. If omitted or null, 2048 is used. */
  @JsonProperty("max_queue_size")
  @Nullable
  public Integer getMaxQueueSize() {
    return maxQueueSize;
  }

  public BatchLogRecordProcessorModel withMaxQueueSize(Integer maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
    return this;
  }

  /** Configure maximum batch size. Value must be positive. If omitted or null, 512 is used. */
  @JsonProperty("max_export_batch_size")
  @Nullable
  public Integer getMaxExportBatchSize() {
    return maxExportBatchSize;
  }

  public BatchLogRecordProcessorModel withMaxExportBatchSize(Integer maxExportBatchSize) {
    this.maxExportBatchSize = maxExportBatchSize;
    return this;
  }

  /** (Required) */
  @JsonProperty("exporter")
  @Nullable
  public LogRecordExporterModel getExporter() {
    return exporter;
  }

  public BatchLogRecordProcessorModel withExporter(LogRecordExporterModel exporter) {
    this.exporter = exporter;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(BatchLogRecordProcessorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("scheduleDelay");
    sb.append('=');
    sb.append(((this.scheduleDelay == null) ? "<null>" : this.scheduleDelay));
    sb.append(',');
    sb.append("exportTimeout");
    sb.append('=');
    sb.append(((this.exportTimeout == null) ? "<null>" : this.exportTimeout));
    sb.append(',');
    sb.append("maxQueueSize");
    sb.append('=');
    sb.append(((this.maxQueueSize == null) ? "<null>" : this.maxQueueSize));
    sb.append(',');
    sb.append("maxExportBatchSize");
    sb.append('=');
    sb.append(((this.maxExportBatchSize == null) ? "<null>" : this.maxExportBatchSize));
    sb.append(',');
    sb.append("exporter");
    sb.append('=');
    sb.append(((this.exporter == null) ? "<null>" : this.exporter));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = ((result * 31) + ((this.scheduleDelay == null) ? 0 : this.scheduleDelay.hashCode()));
    result = ((result * 31) + ((this.exporter == null) ? 0 : this.exporter.hashCode()));
    result = ((result * 31) + ((this.exportTimeout == null) ? 0 : this.exportTimeout.hashCode()));
    result =
        ((result * 31)
            + ((this.maxExportBatchSize == null) ? 0 : this.maxExportBatchSize.hashCode()));
    result = ((result * 31) + ((this.maxQueueSize == null) ? 0 : this.maxQueueSize.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof BatchLogRecordProcessorModel) == false) {
      return false;
    }
    BatchLogRecordProcessorModel rhs = ((BatchLogRecordProcessorModel) other);
    return ((((((this.scheduleDelay == rhs.scheduleDelay)
                        || ((this.scheduleDelay != null)
                            && this.scheduleDelay.equals(rhs.scheduleDelay)))
                    && ((this.exporter == rhs.exporter)
                        || ((this.exporter != null) && this.exporter.equals(rhs.exporter))))
                && ((this.exportTimeout == rhs.exportTimeout)
                    || ((this.exportTimeout != null)
                        && this.exportTimeout.equals(rhs.exportTimeout))))
            && ((this.maxExportBatchSize == rhs.maxExportBatchSize)
                || ((this.maxExportBatchSize != null)
                    && this.maxExportBatchSize.equals(rhs.maxExportBatchSize))))
        && ((this.maxQueueSize == rhs.maxQueueSize)
            || ((this.maxQueueSize != null) && this.maxQueueSize.equals(rhs.maxQueueSize))));
  }
}
