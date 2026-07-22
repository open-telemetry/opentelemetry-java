/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ConsoleMetricExporterModel.DEFAULT_HISTOGRAM_AGGREGATION;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ConsoleMetricExporterModel.TEMPORALITY_PREFERENCE;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({TEMPORALITY_PREFERENCE, DEFAULT_HISTOGRAM_AGGREGATION})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ConsoleMetricExporterModel {

  static final String TEMPORALITY_PREFERENCE = "temporality_preference";
  static final String DEFAULT_HISTOGRAM_AGGREGATION = "default_histogram_aggregation";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(TEMPORALITY_PREFERENCE, ExporterTemporalityPreferenceModel.class);
    STABLE_PROPERTIES.put(
        DEFAULT_HISTOGRAM_AGGREGATION, ExporterDefaultHistogramAggregationModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private ExporterTemporalityPreferenceModel temporalityPreference;
  @Nullable private ExporterDefaultHistogramAggregationModel defaultHistogramAggregation;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

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
  @JsonProperty(TEMPORALITY_PREFERENCE)
  @Nullable
  public ExporterTemporalityPreferenceModel getTemporalityPreference() {
    if (temporalityPreference == null) {
      return ExtensionPropertyUtil.getGraduated(
          TEMPORALITY_PREFERENCE, extensionProperties, ExporterTemporalityPreferenceModel.class);
    }
    return temporalityPreference;
  }

  @JsonProperty(TEMPORALITY_PREFERENCE)
  public ConsoleMetricExporterModel withTemporalityPreference(
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
  @JsonProperty(DEFAULT_HISTOGRAM_AGGREGATION)
  @Nullable
  public ExporterDefaultHistogramAggregationModel getDefaultHistogramAggregation() {
    if (defaultHistogramAggregation == null) {
      return ExtensionPropertyUtil.getGraduated(
          DEFAULT_HISTOGRAM_AGGREGATION,
          extensionProperties,
          ExporterDefaultHistogramAggregationModel.class);
    }
    return defaultHistogramAggregation;
  }

  @JsonProperty(DEFAULT_HISTOGRAM_AGGREGATION)
  public ConsoleMetricExporterModel withDefaultHistogramAggregation(
      ExporterDefaultHistogramAggregationModel defaultHistogramAggregation) {
    this.defaultHistogramAggregation = defaultHistogramAggregation;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public ConsoleMetricExporterModel withExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        Collections.emptyMap(),
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
    return this;
  }

  @Override
  public String toString() {
    return "ConsoleMetricExporterModel{"
        + "temporalityPreference="
        + temporalityPreference
        + ", defaultHistogramAggregation="
        + defaultHistogramAggregation
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.temporalityPreference == null) ? 0 : this.temporalityPreference.hashCode();
    h *= 1000003;
    h ^=
        (this.defaultHistogramAggregation == null)
            ? 0
            : this.defaultHistogramAggregation.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ConsoleMetricExporterModel) {
      ConsoleMetricExporterModel that = (ConsoleMetricExporterModel) o;
      return (this.temporalityPreference == null
              ? that.temporalityPreference == null
              : this.temporalityPreference.equals(that.temporalityPreference))
          && (this.defaultHistogramAggregation == null
              ? that.defaultHistogramAggregation == null
              : this.defaultHistogramAggregation.equals(that.defaultHistogramAggregation))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
