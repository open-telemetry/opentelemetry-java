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
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"output_stream", "temporality_preference", "default_histogram_aggregation"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalOtlpFileMetricExporterModel {

  /**
   * Configure output stream. Values include stdout, or scheme+destination. For example:
   * file:///path/to/file.jsonl. If omitted or null, stdout is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("output_stream")
  @JsonPropertyDescription(
      "Configure output stream. \nValues include stdout, or scheme+destination. For example: file:///path/to/file.jsonl.\nIf omitted or null, stdout is used.\n")
  private String outputStream;

  /** (Can be null) */
  @Nullable
  @JsonProperty("temporality_preference")
  private OtlpHttpMetricExporterModel.ExporterTemporalityPreference temporalityPreference;

  /** (Can be null) */
  @Nullable
  @JsonProperty("default_histogram_aggregation")
  private OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation
      defaultHistogramAggregation;

  /**
   * Configure output stream. Values include stdout, or scheme+destination. For example:
   * file:///path/to/file.jsonl. If omitted or null, stdout is used.
   */
  @JsonProperty("output_stream")
  @Nullable
  public String getOutputStream() {
    return outputStream;
  }

  public ExperimentalOtlpFileMetricExporterModel withOutputStream(String outputStream) {
    this.outputStream = outputStream;
    return this;
  }

  @JsonProperty("temporality_preference")
  @Nullable
  public OtlpHttpMetricExporterModel.ExporterTemporalityPreference getTemporalityPreference() {
    return temporalityPreference;
  }

  public ExperimentalOtlpFileMetricExporterModel withTemporalityPreference(
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

  public ExperimentalOtlpFileMetricExporterModel withDefaultHistogramAggregation(
      OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation defaultHistogramAggregation) {
    this.defaultHistogramAggregation = defaultHistogramAggregation;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalOtlpFileMetricExporterModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("outputStream");
    sb.append('=');
    sb.append(((this.outputStream == null) ? "<null>" : this.outputStream));
    sb.append(',');
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
    result = ((result * 31) + ((this.outputStream == null) ? 0 : this.outputStream.hashCode()));
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
    if ((other instanceof ExperimentalOtlpFileMetricExporterModel) == false) {
      return false;
    }
    ExperimentalOtlpFileMetricExporterModel rhs = ((ExperimentalOtlpFileMetricExporterModel) other);
    return ((((this.temporalityPreference == rhs.temporalityPreference)
                || ((this.temporalityPreference != null)
                    && this.temporalityPreference.equals(rhs.temporalityPreference)))
            && ((this.outputStream == rhs.outputStream)
                || ((this.outputStream != null) && this.outputStream.equals(rhs.outputStream))))
        && ((this.defaultHistogramAggregation == rhs.defaultHistogramAggregation)
            || ((this.defaultHistogramAggregation != null)
                && this.defaultHistogramAggregation.equals(rhs.defaultHistogramAggregation))));
  }
}
