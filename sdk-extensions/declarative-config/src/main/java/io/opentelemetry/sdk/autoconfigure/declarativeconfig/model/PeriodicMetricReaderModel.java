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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"interval", "timeout", "exporter", "producers", "cardinality_limits"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class PeriodicMetricReaderModel {

  /**
   * Configure delay interval (in milliseconds) between start of two consecutive exports. Value must
   * be non-negative. If omitted or null, 60000 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("interval")
  @JsonPropertyDescription(
      "Configure delay interval (in milliseconds) between start of two consecutive exports. \nValue must be non-negative.\nIf omitted or null, 60000 is used.\n")
  private Integer interval;

  /**
   * Configure maximum allowed time (in milliseconds) to export data. Value must be non-negative. A
   * value of 0 indicates no limit (infinity). If omitted or null, 30000 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("timeout")
  @JsonPropertyDescription(
      "Configure maximum allowed time (in milliseconds) to export data. \nValue must be non-negative. A value of 0 indicates no limit (infinity).\nIf omitted or null, 30000 is used.\n")
  private Integer timeout;

  /** (Required) */
  @JsonProperty("exporter")
  @Nonnull
  private PushMetricExporterModel exporter;

  /**
   * Configure metric producers. If omitted, no metric producers are added.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("producers")
  @JsonPropertyDescription(
      "Configure metric producers.\nIf omitted, no metric producers are added.\n")
  private List<MetricProducerModel> producers;

  /** (Can be null) */
  @Nullable
  @JsonProperty("cardinality_limits")
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
    StringBuilder sb = new StringBuilder();
    sb.append(PeriodicMetricReaderModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("interval");
    sb.append('=');
    sb.append(((this.interval == null) ? "<null>" : this.interval));
    sb.append(',');
    sb.append("timeout");
    sb.append('=');
    sb.append(((this.timeout == null) ? "<null>" : this.timeout));
    sb.append(',');
    sb.append("exporter");
    sb.append('=');
    sb.append(((this.exporter == null) ? "<null>" : this.exporter));
    sb.append(',');
    sb.append("producers");
    sb.append('=');
    sb.append(((this.producers == null) ? "<null>" : this.producers));
    sb.append(',');
    sb.append("cardinalityLimits");
    sb.append('=');
    sb.append(((this.cardinalityLimits == null) ? "<null>" : this.cardinalityLimits));
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
    result = ((result * 31) + ((this.interval == null) ? 0 : this.interval.hashCode()));
    result = ((result * 31) + ((this.exporter == null) ? 0 : this.exporter.hashCode()));
    result =
        ((result * 31)
            + ((this.cardinalityLimits == null) ? 0 : this.cardinalityLimits.hashCode()));
    result = ((result * 31) + ((this.timeout == null) ? 0 : this.timeout.hashCode()));
    result = ((result * 31) + ((this.producers == null) ? 0 : this.producers.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof PeriodicMetricReaderModel) == false) {
      return false;
    }
    PeriodicMetricReaderModel rhs = ((PeriodicMetricReaderModel) other);
    return ((((((this.interval == rhs.interval)
                        || ((this.interval != null) && this.interval.equals(rhs.interval)))
                    && ((this.exporter == rhs.exporter)
                        || ((this.exporter != null) && this.exporter.equals(rhs.exporter))))
                && ((this.cardinalityLimits == rhs.cardinalityLimits)
                    || ((this.cardinalityLimits != null)
                        && this.cardinalityLimits.equals(rhs.cardinalityLimits))))
            && ((this.timeout == rhs.timeout)
                || ((this.timeout != null) && this.timeout.equals(rhs.timeout))))
        && ((this.producers == rhs.producers)
            || ((this.producers != null) && this.producers.equals(rhs.producers))));
  }
}
