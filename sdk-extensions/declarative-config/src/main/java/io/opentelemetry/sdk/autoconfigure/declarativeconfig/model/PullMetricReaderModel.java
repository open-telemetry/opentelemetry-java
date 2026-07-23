/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PullMetricReaderModel.CARDINALITY_LIMITS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PullMetricReaderModel.EXPORTER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PullMetricReaderModel.PRODUCERS;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({EXPORTER, PRODUCERS, CARDINALITY_LIMITS})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class PullMetricReaderModel {

  static final String EXPORTER = "exporter";
  static final String PRODUCERS = "producers";
  static final String CARDINALITY_LIMITS = "cardinality_limits";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(EXPORTER, PullMetricExporterModel.class);
    STABLE_PROPERTIES.put(CARDINALITY_LIMITS, CardinalityLimitsModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private PullMetricExporterModel exporter;
  @Nullable private List<MetricProducerModel> producers;
  @Nullable private CardinalityLimitsModel cardinalityLimits;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure exporter.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty(EXPORTER)
  @Nullable
  public PullMetricExporterModel getExporter() {
    if (exporter == null) {
      return ExtensionPropertyUtil.getGraduated(
          EXPORTER, extensionProperties, PullMetricExporterModel.class);
    }
    return exporter;
  }

  @JsonProperty(EXPORTER)
  public PullMetricReaderModel withExporter(PullMetricExporterModel exporter) {
    this.exporter = exporter;
    return this;
  }

  /**
   * Configure metric producers.
   *
   * <p>If omitted, no metric producers are added.
   */
  @JsonProperty(PRODUCERS)
  @Nullable
  public List<MetricProducerModel> getProducers() {
    return producers;
  }

  @JsonProperty(PRODUCERS)
  public PullMetricReaderModel withProducers(List<MetricProducerModel> producers) {
    this.producers = producers;
    return this;
  }

  /**
   * Configure cardinality limits.
   *
   * <p>If omitted, default values as described in CardinalityLimits are used.
   */
  @JsonProperty(CARDINALITY_LIMITS)
  @Nullable
  public CardinalityLimitsModel getCardinalityLimits() {
    if (cardinalityLimits == null) {
      return ExtensionPropertyUtil.getGraduated(
          CARDINALITY_LIMITS, extensionProperties, CardinalityLimitsModel.class);
    }
    return cardinalityLimits;
  }

  @JsonProperty(CARDINALITY_LIMITS)
  public PullMetricReaderModel withCardinalityLimits(CardinalityLimitsModel cardinalityLimits) {
    this.cardinalityLimits = cardinalityLimits;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public PullMetricReaderModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "PullMetricReaderModel{"
        + "exporter="
        + exporter
        + ", producers="
        + producers
        + ", cardinalityLimits="
        + cardinalityLimits
        + ", extensionProperties="
        + extensionProperties
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
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
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
              : this.cardinalityLimits.equals(that.cardinalityLimits))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
