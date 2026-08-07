/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PeriodicMetricReaderModel.CARDINALITY_LIMITS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PeriodicMetricReaderModel.EXPORTER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PeriodicMetricReaderModel.INTERVAL;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PeriodicMetricReaderModel.PRODUCERS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PeriodicMetricReaderModel.TIMEOUT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.PeriodicMetricReaderModelAccessor.EXPERIMENTAL_PROPERTIES;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({INTERVAL, TIMEOUT, EXPORTER, PRODUCERS, CARDINALITY_LIMITS})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class PeriodicMetricReaderModel {

  static final String INTERVAL = "interval";
  static final String TIMEOUT = "timeout";
  static final String EXPORTER = "exporter";
  static final String PRODUCERS = "producers";
  static final String CARDINALITY_LIMITS = "cardinality_limits";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(INTERVAL, Integer.class);
    STABLE_PROPERTIES.put(TIMEOUT, Integer.class);
    STABLE_PROPERTIES.put(EXPORTER, PushMetricExporterModel.class);
    STABLE_PROPERTIES.put(CARDINALITY_LIMITS, CardinalityLimitsModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private Integer interval;
  @Nullable private Integer timeout;
  @Nullable private PushMetricExporterModel exporter;
  @Nullable private List<MetricProducerModel> producers;
  @Nullable private CardinalityLimitsModel cardinalityLimits;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure delay interval (in milliseconds) between start of two consecutive exports.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, 60000 is used.
   */
  @JsonProperty(INTERVAL)
  @Nullable
  public Integer getInterval() {
    if (interval == null) {
      return ExtensionPropertyUtil.getGraduated(INTERVAL, extensionProperties, Integer.class);
    }
    return interval;
  }

  @JsonProperty(INTERVAL)
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
  @JsonProperty(TIMEOUT)
  @Nullable
  public Integer getTimeout() {
    if (timeout == null) {
      return ExtensionPropertyUtil.getGraduated(TIMEOUT, extensionProperties, Integer.class);
    }
    return timeout;
  }

  @JsonProperty(TIMEOUT)
  public PeriodicMetricReaderModel withTimeout(Integer timeout) {
    this.timeout = timeout;
    return this;
  }

  /**
   * Configure exporter.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty(EXPORTER)
  @Nullable
  public PushMetricExporterModel getExporter() {
    if (exporter == null) {
      return ExtensionPropertyUtil.getGraduated(
          EXPORTER, extensionProperties, PushMetricExporterModel.class);
    }
    return exporter;
  }

  @JsonProperty(EXPORTER)
  public PeriodicMetricReaderModel withExporter(PushMetricExporterModel exporter) {
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
  public PeriodicMetricReaderModel withProducers(List<MetricProducerModel> producers) {
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
  public PeriodicMetricReaderModel withCardinalityLimits(CardinalityLimitsModel cardinalityLimits) {
    this.cardinalityLimits = cardinalityLimits;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public PeriodicMetricReaderModel withExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        EXPERIMENTAL_PROPERTIES,
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
    return this;
  }

  @Override
  public String toString() {
    return "PeriodicMetricReaderModel{"
        + "interval="
        + interval
        + ", timeout="
        + timeout
        + ", exporter="
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
    h ^= (this.interval == null) ? 0 : this.interval.hashCode();
    h *= 1000003;
    h ^= (this.timeout == null) ? 0 : this.timeout.hashCode();
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
    if (o instanceof PeriodicMetricReaderModel) {
      PeriodicMetricReaderModel that = (PeriodicMetricReaderModel) o;
      return (this.interval == null ? that.interval == null : this.interval.equals(that.interval))
          && (this.timeout == null ? that.timeout == null : this.timeout.equals(that.timeout))
          && (this.exporter == null ? that.exporter == null : this.exporter.equals(that.exporter))
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
