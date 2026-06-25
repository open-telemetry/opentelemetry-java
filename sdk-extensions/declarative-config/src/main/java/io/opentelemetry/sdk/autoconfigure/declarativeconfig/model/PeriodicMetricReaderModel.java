/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "interval",
  "timeout",
  "max_export_batch_size/development",
  "exporter",
  "producers",
  "cardinality_limits"
})
@Generated("jsonschema2pojo")
public class PeriodicMetricReaderModel {

  /**
   * Configure delay interval (in milliseconds) between start of two consecutive exports. Value must
   * be non-negative. If omitted or null, 60000 is used.
   */
  @JsonProperty("interval")
  @JsonPropertyDescription(
      "Configure delay interval (in milliseconds) between start of two consecutive exports. \nValue must be non-negative.\nIf omitted or null, 60000 is used.\n")
  @Nullable
  private Integer interval;

  /**
   * Configure maximum allowed time (in milliseconds) to export data. Value must be non-negative. A
   * value of 0 indicates no limit (infinity). If omitted or null, 30000 is used.
   */
  @JsonProperty("timeout")
  @JsonPropertyDescription(
      "Configure maximum allowed time (in milliseconds) to export data. \nValue must be non-negative. A value of 0 indicates no limit (infinity).\nIf omitted or null, 30000 is used.\n")
  @Nullable
  private Integer timeout;

  /** Configure maximum export batch size. If omitted or null, no limit is used. */
  @JsonProperty("max_export_batch_size/development")
  @JsonPropertyDescription(
      "Configure maximum export batch size.\nIf omitted or null, no limit is used.\n")
  @Nullable
  private Integer maxExportBatchSizeDevelopment;

  /** (Required) */
  @JsonProperty("exporter")
  @Nullable
  private PushMetricExporterModel exporter;

  /** Configure metric producers. If omitted, no metric producers are added. */
  @JsonProperty("producers")
  @JsonPropertyDescription(
      "Configure metric producers.\nIf omitted, no metric producers are added.\n")
  @Nullable
  private List<MetricProducerModel> producers;

  @JsonProperty("cardinality_limits")
  @Nullable
  private CardinalityLimitsModel cardinalityLimits;

  /**
   * Configure delay interval (in milliseconds) between start of two consecutive exports. Value must
   * be non-negative. If omitted or null, 60000 is used.
   */
  @JsonProperty("interval")
  @Nullable
  public Integer getInterval() {
    return interval;
  }

  public PeriodicMetricReaderModel withInterval(Integer interval) {
    this.interval = interval;
    return this;
  }

  /**
   * Configure maximum allowed time (in milliseconds) to export data. Value must be non-negative. A
   * value of 0 indicates no limit (infinity). If omitted or null, 30000 is used.
   */
  @JsonProperty("timeout")
  @Nullable
  public Integer getTimeout() {
    return timeout;
  }

  public PeriodicMetricReaderModel withTimeout(Integer timeout) {
    this.timeout = timeout;
    return this;
  }

  /** Configure maximum export batch size. If omitted or null, no limit is used. */
  @JsonProperty("max_export_batch_size/development")
  @Nullable
  public Integer getMaxExportBatchSizeDevelopment() {
    return maxExportBatchSizeDevelopment;
  }

  public PeriodicMetricReaderModel withMaxExportBatchSizeDevelopment(
      Integer maxExportBatchSizeDevelopment) {
    this.maxExportBatchSizeDevelopment = maxExportBatchSizeDevelopment;
    return this;
  }

  /** (Required) */
  @JsonProperty("exporter")
  @Nullable
  public PushMetricExporterModel getExporter() {
    return exporter;
  }

  public PeriodicMetricReaderModel withExporter(PushMetricExporterModel exporter) {
    this.exporter = exporter;
    return this;
  }

  /** Configure metric producers. If omitted, no metric producers are added. */
  @JsonProperty("producers")
  @Nullable
  public List<MetricProducerModel> getProducers() {
    return producers;
  }

  public PeriodicMetricReaderModel withProducers(List<MetricProducerModel> producers) {
    this.producers = producers;
    return this;
  }

  @JsonProperty("cardinality_limits")
  @Nullable
  public CardinalityLimitsModel getCardinalityLimits() {
    return cardinalityLimits;
  }

  public PeriodicMetricReaderModel withCardinalityLimits(CardinalityLimitsModel cardinalityLimits) {
    this.cardinalityLimits = cardinalityLimits;
    return this;
  }

  @Override
  public String toString() {
    return "PeriodicMetricReaderModel{"
        + "interval="
        + interval
        + ", timeout="
        + timeout
        + ", maxExportBatchSizeDevelopment="
        + maxExportBatchSizeDevelopment
        + ", exporter="
        + exporter
        + ", producers="
        + producers
        + ", cardinalityLimits="
        + cardinalityLimits
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.interval == null) ? 0 : this.interval.hashCode();
    h *= 1000003;
    h ^= (this.timeout == null) ? 0 : this.timeout.hashCode();
    h *= 1000003;
    h ^=
        (this.maxExportBatchSizeDevelopment == null)
            ? 0
            : this.maxExportBatchSizeDevelopment.hashCode();
    h *= 1000003;
    h ^= (this.exporter == null) ? 0 : this.exporter.hashCode();
    h *= 1000003;
    h ^= (this.producers == null) ? 0 : this.producers.hashCode();
    h *= 1000003;
    h ^= (this.cardinalityLimits == null) ? 0 : this.cardinalityLimits.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof PeriodicMetricReaderModel) {
      PeriodicMetricReaderModel that = (PeriodicMetricReaderModel) o;
      return (this.interval == null ? that.interval == null : this.interval.equals(that.interval))
          && (this.timeout == null ? that.timeout == null : this.timeout.equals(that.timeout))
          && (this.maxExportBatchSizeDevelopment == null
              ? that.maxExportBatchSizeDevelopment == null
              : this.maxExportBatchSizeDevelopment.equals(that.maxExportBatchSizeDevelopment))
          && (this.exporter == null ? that.exporter == null : this.exporter.equals(that.exporter))
          && (this.producers == null
              ? that.producers == null
              : this.producers.equals(that.producers))
          && (this.cardinalityLimits == null
              ? that.cardinalityLimits == null
              : this.cardinalityLimits.equals(that.cardinalityLimits));
    }
    return false;
  }
}
