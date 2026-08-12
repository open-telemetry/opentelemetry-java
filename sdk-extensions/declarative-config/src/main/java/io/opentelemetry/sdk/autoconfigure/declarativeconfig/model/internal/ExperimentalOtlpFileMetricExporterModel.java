/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExporterDefaultHistogramAggregationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExporterTemporalityPreferenceModel;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"output_stream", "temporality_preference", "default_histogram_aggregation"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalOtlpFileMetricExporterModel {

  @Nullable private String outputStream;
  @Nullable private ExporterTemporalityPreferenceModel temporalityPreference;
  @Nullable private ExporterDefaultHistogramAggregationModel defaultHistogramAggregation;

  /**
   * Configure output stream.
   *
   * <p>Values include stdout, or scheme+destination. For example: file:///path/to/file.jsonl.
   *
   * <p>If omitted or null, stdout is used.
   */
  @JsonProperty("output_stream")
  @Nullable
  public String getOutputStream() {
    return outputStream;
  }

  @JsonProperty("output_stream")
  public ExperimentalOtlpFileMetricExporterModel withOutputStream(String outputStream) {
    this.outputStream = outputStream;
    return this;
  }

  /**
   * Configure temporality preference.
   *
   * <p>Values include:
   *
   * <p>* cumulative: Use cumulative aggregation temporality for all instrument types.
   *
   * <p>* delta: Use delta aggregation for all instrument types except up down counter and
   * asynchronous up down counter.
   *
   * <p>* low_memory: Use delta aggregation temporality for counter and histogram instrument types.
   * Use cumulative aggregation temporality for all other instrument types.
   *
   * <p>If omitted, cumulative is used.
   */
  @JsonProperty("temporality_preference")
  @Nullable
  public ExporterTemporalityPreferenceModel getTemporalityPreference() {
    return temporalityPreference;
  }

  @JsonProperty("temporality_preference")
  public ExperimentalOtlpFileMetricExporterModel withTemporalityPreference(
      ExporterTemporalityPreferenceModel temporalityPreference) {
    this.temporalityPreference = temporalityPreference;
    return this;
  }

  /**
   * Configure default histogram aggregation.
   *
   * <p>Values include:
   *
   * <p>* base2_exponential_bucket_histogram: Use base2 exponential histogram as the default
   * aggregation for histogram instruments.
   *
   * <p>* explicit_bucket_histogram: Use explicit bucket histogram as the default aggregation for
   * histogram instruments.
   *
   * <p>If omitted, explicit_bucket_histogram is used.
   */
  @JsonProperty("default_histogram_aggregation")
  @Nullable
  public ExporterDefaultHistogramAggregationModel getDefaultHistogramAggregation() {
    return defaultHistogramAggregation;
  }

  @JsonProperty("default_histogram_aggregation")
  public ExperimentalOtlpFileMetricExporterModel withDefaultHistogramAggregation(
      ExporterDefaultHistogramAggregationModel defaultHistogramAggregation) {
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
