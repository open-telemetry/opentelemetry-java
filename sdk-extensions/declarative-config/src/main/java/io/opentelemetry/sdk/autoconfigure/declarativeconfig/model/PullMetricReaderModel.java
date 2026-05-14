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
@JsonPropertyOrder({"exporter", "producers", "cardinality_limits"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class PullMetricReaderModel {

  /** (Required) */
  @JsonProperty("exporter")
  @Nonnull
  private PullMetricExporterModel exporter;

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

  /** (Required) */
  @JsonProperty("exporter")
  @Nullable
  public PullMetricExporterModel getExporter() {
    return exporter;
  }

  public PullMetricReaderModel withExporter(PullMetricExporterModel exporter) {
    this.exporter = exporter;
    return this;
  }

  /** Configure metric producers. If omitted, no metric producers are added. */
  @JsonProperty("producers")
  @Nullable
  public List<MetricProducerModel> getProducers() {
    return producers;
  }

  public PullMetricReaderModel withProducers(List<MetricProducerModel> producers) {
    this.producers = producers;
    return this;
  }

  @JsonProperty("cardinality_limits")
  @Nullable
  public CardinalityLimitsModel getCardinalityLimits() {
    return cardinalityLimits;
  }

  public PullMetricReaderModel withCardinalityLimits(CardinalityLimitsModel cardinalityLimits) {
    this.cardinalityLimits = cardinalityLimits;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(PullMetricReaderModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
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
    result =
        ((result * 31)
            + ((this.cardinalityLimits == null) ? 0 : this.cardinalityLimits.hashCode()));
    result = ((result * 31) + ((this.exporter == null) ? 0 : this.exporter.hashCode()));
    result = ((result * 31) + ((this.producers == null) ? 0 : this.producers.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof PullMetricReaderModel) == false) {
      return false;
    }
    PullMetricReaderModel rhs = ((PullMetricReaderModel) other);
    return ((((this.cardinalityLimits == rhs.cardinalityLimits)
                || ((this.cardinalityLimits != null)
                    && this.cardinalityLimits.equals(rhs.cardinalityLimits)))
            && ((this.exporter == rhs.exporter)
                || ((this.exporter != null) && this.exporter.equals(rhs.exporter))))
        && ((this.producers == rhs.producers)
            || ((this.producers != null) && this.producers.equals(rhs.producers))));
  }
}
