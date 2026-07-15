/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class PeriodicMetricReaderModel {

  @Nullable private Integer interval;
  @Nullable private Integer timeout;
  @Nullable private Integer maxExportBatchSizeDevelopment;
  @Nullable private PushMetricExporterModel exporter;
  @Nullable private List<MetricProducerModel> producers;
  @Nullable private CardinalityLimitsModel cardinalityLimits;

  /**
   * Configure delay interval (in milliseconds) between start of two consecutive exports.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, 60000 is used.
   */
  @JsonProperty("interval")
  @Nullable
  public Integer getInterval() {
    return interval;
  }

  @JsonProperty("interval")
  public PeriodicMetricReaderModel withInterval(Integer interval) {
    this.interval = interval;
    return this;
  }

  /**
   * Configure maximum allowed time (in milliseconds) to export data.
   *
   * <p>Value must be non-negative. A value of 0 indicates no limit (infinity).
   *
   * <p>If omitted or null, 30000 is used.
   */
  @JsonProperty("timeout")
  @Nullable
  public Integer getTimeout() {
    return timeout;
  }

  @JsonProperty("timeout")
  public PeriodicMetricReaderModel withTimeout(Integer timeout) {
    this.timeout = timeout;
    return this;
  }

  /**
   * Configure maximum export batch size.
   *
   * <p>If omitted or null, no limit is used.
   */
  @JsonProperty("max_export_batch_size/development")
  @Nullable
  public Integer getMaxExportBatchSizeDevelopment() {
    return maxExportBatchSizeDevelopment;
  }

  @JsonProperty("max_export_batch_size/development")
  public PeriodicMetricReaderModel withMaxExportBatchSizeDevelopment(
      Integer maxExportBatchSizeDevelopment) {
    this.maxExportBatchSizeDevelopment = maxExportBatchSizeDevelopment;
    return this;
  }

  /**
   * Configure exporter.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("exporter")
  @Nullable
  public PushMetricExporterModel getExporter() {
    return exporter;
  }

  @JsonProperty("exporter")
  public PeriodicMetricReaderModel withExporter(PushMetricExporterModel exporter) {
    this.exporter = exporter;
    return this;
  }

  /**
   * Configure metric producers.
   *
   * <p>If omitted, no metric producers are added.
   */
  @JsonProperty("producers")
  @Nullable
  public List<MetricProducerModel> getProducers() {
    return producers;
  }

  @JsonProperty("producers")
  public PeriodicMetricReaderModel withProducers(List<MetricProducerModel> producers) {
    this.producers = producers;
    return this;
  }

  /**
   * Configure cardinality limits.
   *
   * <p>If omitted, default values as described in CardinalityLimits are used.
   */
  @JsonProperty("cardinality_limits")
  @Nullable
  public CardinalityLimitsModel getCardinalityLimits() {
    return cardinalityLimits;
  }

  @JsonProperty("cardinality_limits")
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
