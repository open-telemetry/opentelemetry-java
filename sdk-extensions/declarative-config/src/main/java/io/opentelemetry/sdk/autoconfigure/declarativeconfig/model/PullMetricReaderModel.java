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
@JsonPropertyOrder({"exporter", "producers", "cardinality_limits"})
@Generated("jsonschema2pojo")
public class PullMetricReaderModel {

  /** (Required) */
  @JsonProperty("exporter")
  @Nullable
  private PullMetricExporterModel exporter;

  /** Configure metric producers. If omitted, no metric producers are added. */
  @JsonProperty("producers")
  @JsonPropertyDescription(
      "Configure metric producers.\nIf omitted, no metric producers are added.\n")
  @Nullable
  private List<MetricProducerModel> producers;

  @JsonProperty("cardinality_limits")
  @Nullable
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
    return "PullMetricReaderModel{"
        + "exporter="
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
    if (o instanceof PullMetricReaderModel) {
      PullMetricReaderModel that = (PullMetricReaderModel) o;
      return (this.exporter == null ? that.exporter == null : this.exporter.equals(that.exporter))
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
