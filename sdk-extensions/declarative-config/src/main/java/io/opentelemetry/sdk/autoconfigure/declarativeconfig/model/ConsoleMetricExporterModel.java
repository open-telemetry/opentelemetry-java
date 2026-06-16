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
@JsonPropertyOrder({"temporality_preference", "default_histogram_aggregation"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ConsoleMetricExporterModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("temporality_preference")
  private OtlpHttpMetricExporterModel.ExporterTemporalityPreference temporalityPreference;

  /** (Can be null) */
  @Nullable
  @JsonProperty("default_histogram_aggregation")
  private OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation
      defaultHistogramAggregation;

  @JsonProperty("temporality_preference")
  @Nullable
  public OtlpHttpMetricExporterModel.ExporterTemporalityPreference getTemporalityPreference() {
    return temporalityPreference;
  }

  public ConsoleMetricExporterModel withTemporalityPreference(
      OtlpHttpMetricExporterModel.ExporterTemporalityPreference temporalityPreference) {
    this.temporalityPreference = temporalityPreference;
    return this;
  }

  @JsonProperty("default_histogram_aggregation")
  @Nullable
  public OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation
      getDefaultHistogramAggregation() {
    return defaultHistogramAggregation;
  }

  public ConsoleMetricExporterModel withDefaultHistogramAggregation(
      OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation defaultHistogramAggregation) {
    this.defaultHistogramAggregation = defaultHistogramAggregation;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ConsoleMetricExporterModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("temporalityPreference");
    sb.append('=');
    sb.append(((this.temporalityPreference == null) ? "<null>" : this.temporalityPreference));
    sb.append(',');
    sb.append("defaultHistogramAggregation");
    sb.append('=');
    sb.append(
        ((this.defaultHistogramAggregation == null) ? "<null>" : this.defaultHistogramAggregation));
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
            + ((this.temporalityPreference == null) ? 0 : this.temporalityPreference.hashCode()));
    result =
        ((result * 31)
            + ((this.defaultHistogramAggregation == null)
                ? 0
                : this.defaultHistogramAggregation.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ConsoleMetricExporterModel) == false) {
      return false;
    }
    ConsoleMetricExporterModel rhs = ((ConsoleMetricExporterModel) other);
    return (((this.temporalityPreference == rhs.temporalityPreference)
            || ((this.temporalityPreference != null)
                && this.temporalityPreference.equals(rhs.temporalityPreference)))
        && ((this.defaultHistogramAggregation == rhs.defaultHistogramAggregation)
            || ((this.defaultHistogramAggregation != null)
                && this.defaultHistogramAggregation.equals(rhs.defaultHistogramAggregation))));
  }
}
