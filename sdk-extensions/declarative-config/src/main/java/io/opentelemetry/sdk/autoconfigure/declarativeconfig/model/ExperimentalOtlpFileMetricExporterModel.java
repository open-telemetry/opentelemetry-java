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
public class ExperimentalOtlpFileMetricExporterModel {

  /**
   * Configure output stream. Values include stdout, or scheme+destination. For example:
   * file:///path/to/file.jsonl. If omitted or null, stdout is used.
   */
  @JsonProperty("output_stream")
  @JsonPropertyDescription(
      "Configure output stream. \nValues include stdout, or scheme+destination. For example: file:///path/to/file.jsonl.\nIf omitted or null, stdout is used.\n")
  @Nullable
  private String outputStream;

  @JsonProperty("temporality_preference")
  @Nullable
  private OtlpHttpMetricExporterModel.ExporterTemporalityPreference temporalityPreference;

  @JsonProperty("default_histogram_aggregation")
  @Nullable
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
    return "ExperimentalOtlpFileMetricExporterModel{"
        + "outputStream="
        + outputStream
        + ", temporalityPreference="
        + temporalityPreference
        + ", defaultHistogramAggregation="
        + defaultHistogramAggregation
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.outputStream == null) ? 0 : this.outputStream.hashCode();
    h *= 1000003;
    h ^= (this.temporalityPreference == null) ? 0 : this.temporalityPreference.hashCode();
    h *= 1000003;
    h ^=
        (this.defaultHistogramAggregation == null)
            ? 0
            : this.defaultHistogramAggregation.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalOtlpFileMetricExporterModel) {
      ExperimentalOtlpFileMetricExporterModel that = (ExperimentalOtlpFileMetricExporterModel) o;
      return (this.outputStream == null
              ? that.outputStream == null
              : this.outputStream.equals(that.outputStream))
          && (this.temporalityPreference == null
              ? that.temporalityPreference == null
              : this.temporalityPreference.equals(that.temporalityPreference))
          && (this.defaultHistogramAggregation == null
              ? that.defaultHistogramAggregation == null
              : this.defaultHistogramAggregation.equals(that.defaultHistogramAggregation));
    }
    return false;
  }
}
