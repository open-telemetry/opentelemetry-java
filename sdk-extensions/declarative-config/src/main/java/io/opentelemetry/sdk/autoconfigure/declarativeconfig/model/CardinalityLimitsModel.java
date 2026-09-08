/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.CardinalityLimitsModel.COUNTER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.CardinalityLimitsModel.DEFAULT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.CardinalityLimitsModel.GAUGE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.CardinalityLimitsModel.HISTOGRAM;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.CardinalityLimitsModel.OBSERVABLE_COUNTER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.CardinalityLimitsModel.OBSERVABLE_GAUGE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.CardinalityLimitsModel.OBSERVABLE_UP_DOWN_COUNTER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.CardinalityLimitsModel.UP_DOWN_COUNTER;

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
@JsonPropertyOrder({
  DEFAULT,
  COUNTER,
  GAUGE,
  HISTOGRAM,
  OBSERVABLE_COUNTER,
  OBSERVABLE_GAUGE,
  OBSERVABLE_UP_DOWN_COUNTER,
  UP_DOWN_COUNTER
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class CardinalityLimitsModel {

  static final String DEFAULT = "default";
  static final String COUNTER = "counter";
  static final String GAUGE = "gauge";
  static final String HISTOGRAM = "histogram";
  static final String OBSERVABLE_COUNTER = "observable_counter";
  static final String OBSERVABLE_GAUGE = "observable_gauge";
  static final String OBSERVABLE_UP_DOWN_COUNTER = "observable_up_down_counter";
  static final String UP_DOWN_COUNTER = "up_down_counter";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(DEFAULT, Integer.class);
    STABLE_PROPERTIES.put(COUNTER, Integer.class);
    STABLE_PROPERTIES.put(GAUGE, Integer.class);
    STABLE_PROPERTIES.put(HISTOGRAM, Integer.class);
    STABLE_PROPERTIES.put(OBSERVABLE_COUNTER, Integer.class);
    STABLE_PROPERTIES.put(OBSERVABLE_GAUGE, Integer.class);
    STABLE_PROPERTIES.put(OBSERVABLE_UP_DOWN_COUNTER, Integer.class);
    STABLE_PROPERTIES.put(UP_DOWN_COUNTER, Integer.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private Integer _default;
  @Nullable private Integer counter;
  @Nullable private Integer gauge;
  @Nullable private Integer histogram;
  @Nullable private Integer observableCounter;
  @Nullable private Integer observableGauge;
  @Nullable private Integer observableUpDownCounter;
  @Nullable private Integer upDownCounter;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure default cardinality limit for all instrument types.
   *
   * <p>Instrument-specific cardinality limits take priority.
   *
   * <p>If omitted or null, 2000 is used.
   */
  @JsonProperty(DEFAULT)
  @Nullable
  public Integer getDefault() {
    if (_default == null) {
      return ExtensionPropertyUtil.getGraduated(DEFAULT, extensionProperties, Integer.class);
    }
    return _default;
  }

  @JsonProperty(DEFAULT)
  public CardinalityLimitsModel setDefault(Integer _default) {
    this._default = _default;
    return this;
  }

  /**
   * Configure default cardinality limit for counter instruments.
   *
   * <p>If omitted or null, the value from .default is used.
   */
  @JsonProperty(COUNTER)
  @Nullable
  public Integer getCounter() {
    if (counter == null) {
      return ExtensionPropertyUtil.getGraduated(COUNTER, extensionProperties, Integer.class);
    }
    return counter;
  }

  @JsonProperty(COUNTER)
  public CardinalityLimitsModel setCounter(Integer counter) {
    this.counter = counter;
    return this;
  }

  /**
   * Configure default cardinality limit for gauge instruments.
   *
   * <p>If omitted or null, the value from .default is used.
   */
  @JsonProperty(GAUGE)
  @Nullable
  public Integer getGauge() {
    if (gauge == null) {
      return ExtensionPropertyUtil.getGraduated(GAUGE, extensionProperties, Integer.class);
    }
    return gauge;
  }

  @JsonProperty(GAUGE)
  public CardinalityLimitsModel setGauge(Integer gauge) {
    this.gauge = gauge;
    return this;
  }

  /**
   * Configure default cardinality limit for histogram instruments.
   *
   * <p>If omitted or null, the value from .default is used.
   */
  @JsonProperty(HISTOGRAM)
  @Nullable
  public Integer getHistogram() {
    if (histogram == null) {
      return ExtensionPropertyUtil.getGraduated(HISTOGRAM, extensionProperties, Integer.class);
    }
    return histogram;
  }

  @JsonProperty(HISTOGRAM)
  public CardinalityLimitsModel setHistogram(Integer histogram) {
    this.histogram = histogram;
    return this;
  }

  /**
   * Configure default cardinality limit for observable_counter instruments.
   *
   * <p>If omitted or null, the value from .default is used.
   */
  @JsonProperty(OBSERVABLE_COUNTER)
  @Nullable
  public Integer getObservableCounter() {
    if (observableCounter == null) {
      return ExtensionPropertyUtil.getGraduated(
          OBSERVABLE_COUNTER, extensionProperties, Integer.class);
    }
    return observableCounter;
  }

  @JsonProperty(OBSERVABLE_COUNTER)
  public CardinalityLimitsModel setObservableCounter(Integer observableCounter) {
    this.observableCounter = observableCounter;
    return this;
  }

  /**
   * Configure default cardinality limit for observable_gauge instruments.
   *
   * <p>If omitted or null, the value from .default is used.
   */
  @JsonProperty(OBSERVABLE_GAUGE)
  @Nullable
  public Integer getObservableGauge() {
    if (observableGauge == null) {
      return ExtensionPropertyUtil.getGraduated(
          OBSERVABLE_GAUGE, extensionProperties, Integer.class);
    }
    return observableGauge;
  }

  @JsonProperty(OBSERVABLE_GAUGE)
  public CardinalityLimitsModel setObservableGauge(Integer observableGauge) {
    this.observableGauge = observableGauge;
    return this;
  }

  /**
   * Configure default cardinality limit for observable_up_down_counter instruments.
   *
   * <p>If omitted or null, the value from .default is used.
   */
  @JsonProperty(OBSERVABLE_UP_DOWN_COUNTER)
  @Nullable
  public Integer getObservableUpDownCounter() {
    if (observableUpDownCounter == null) {
      return ExtensionPropertyUtil.getGraduated(
          OBSERVABLE_UP_DOWN_COUNTER, extensionProperties, Integer.class);
    }
    return observableUpDownCounter;
  }

  @JsonProperty(OBSERVABLE_UP_DOWN_COUNTER)
  public CardinalityLimitsModel setObservableUpDownCounter(Integer observableUpDownCounter) {
    this.observableUpDownCounter = observableUpDownCounter;
    return this;
  }

  /**
   * Configure default cardinality limit for up_down_counter instruments.
   *
   * <p>If omitted or null, the value from .default is used.
   */
  @JsonProperty(UP_DOWN_COUNTER)
  @Nullable
  public Integer getUpDownCounter() {
    if (upDownCounter == null) {
      return ExtensionPropertyUtil.getGraduated(
          UP_DOWN_COUNTER, extensionProperties, Integer.class);
    }
    return upDownCounter;
  }

  @JsonProperty(UP_DOWN_COUNTER)
  public CardinalityLimitsModel setUpDownCounter(Integer upDownCounter) {
    this.upDownCounter = upDownCounter;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public CardinalityLimitsModel setExtensionProperty(String name, @Nullable Object value) {
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
    return "CardinalityLimitsModel{"
        + "_default="
        + _default
        + ", counter="
        + counter
        + ", gauge="
        + gauge
        + ", histogram="
        + histogram
        + ", observableCounter="
        + observableCounter
        + ", observableGauge="
        + observableGauge
        + ", observableUpDownCounter="
        + observableUpDownCounter
        + ", upDownCounter="
        + upDownCounter
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.getDefault() == null) ? 0 : this.getDefault().hashCode();
    h *= 1000003;
    h ^= (this.getCounter() == null) ? 0 : this.getCounter().hashCode();
    h *= 1000003;
    h ^= (this.getGauge() == null) ? 0 : this.getGauge().hashCode();
    h *= 1000003;
    h ^= (this.getHistogram() == null) ? 0 : this.getHistogram().hashCode();
    h *= 1000003;
    h ^= (this.getObservableCounter() == null) ? 0 : this.getObservableCounter().hashCode();
    h *= 1000003;
    h ^= (this.getObservableGauge() == null) ? 0 : this.getObservableGauge().hashCode();
    h *= 1000003;
    h ^=
        (this.getObservableUpDownCounter() == null)
            ? 0
            : this.getObservableUpDownCounter().hashCode();
    h *= 1000003;
    h ^= (this.getUpDownCounter() == null) ? 0 : this.getUpDownCounter().hashCode();
    h *= 1000003;
    h ^= (this.getExtensionProperties() == null) ? 0 : this.getExtensionProperties().hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof CardinalityLimitsModel) {
      CardinalityLimitsModel that = (CardinalityLimitsModel) o;
      return (this.getDefault() == null
              ? that.getDefault() == null
              : this.getDefault().equals(that.getDefault()))
          && (this.getCounter() == null
              ? that.getCounter() == null
              : this.getCounter().equals(that.getCounter()))
          && (this.getGauge() == null
              ? that.getGauge() == null
              : this.getGauge().equals(that.getGauge()))
          && (this.getHistogram() == null
              ? that.getHistogram() == null
              : this.getHistogram().equals(that.getHistogram()))
          && (this.getObservableCounter() == null
              ? that.getObservableCounter() == null
              : this.getObservableCounter().equals(that.getObservableCounter()))
          && (this.getObservableGauge() == null
              ? that.getObservableGauge() == null
              : this.getObservableGauge().equals(that.getObservableGauge()))
          && (this.getObservableUpDownCounter() == null
              ? that.getObservableUpDownCounter() == null
              : this.getObservableUpDownCounter().equals(that.getObservableUpDownCounter()))
          && (this.getUpDownCounter() == null
              ? that.getUpDownCounter() == null
              : this.getUpDownCounter().equals(that.getUpDownCounter()))
          && (this.getExtensionProperties() == null
              ? that.getExtensionProperties() == null
              : this.getExtensionProperties().equals(that.getExtensionProperties()));
    }
    return false;
  }
}
