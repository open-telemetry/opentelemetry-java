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
@JsonPropertyOrder({
  "default",
  "counter",
  "gauge",
  "histogram",
  "observable_counter",
  "observable_gauge",
  "observable_up_down_counter",
  "up_down_counter"
})
@Generated("jsonschema2pojo")
public class CardinalityLimitsModel {

  /**
   * Configure default cardinality limit for all instrument types. Instrument-specific cardinality
   * limits take priority. If omitted or null, 2000 is used.
   */
  @JsonProperty("default")
  @JsonPropertyDescription(
      "Configure default cardinality limit for all instrument types.\nInstrument-specific cardinality limits take priority.\nIf omitted or null, 2000 is used.\n")
  @Nullable
  private Integer _default;

  /**
   * Configure default cardinality limit for counter instruments. If omitted or null, the value from
   * .default is used.
   */
  @JsonProperty("counter")
  @JsonPropertyDescription(
      "Configure default cardinality limit for counter instruments.\nIf omitted or null, the value from .default is used.\n")
  @Nullable
  private Integer counter;

  /**
   * Configure default cardinality limit for gauge instruments. If omitted or null, the value from
   * .default is used.
   */
  @JsonProperty("gauge")
  @JsonPropertyDescription(
      "Configure default cardinality limit for gauge instruments.\nIf omitted or null, the value from .default is used.\n")
  @Nullable
  private Integer gauge;

  /**
   * Configure default cardinality limit for histogram instruments. If omitted or null, the value
   * from .default is used.
   */
  @JsonProperty("histogram")
  @JsonPropertyDescription(
      "Configure default cardinality limit for histogram instruments.\nIf omitted or null, the value from .default is used.\n")
  @Nullable
  private Integer histogram;

  /**
   * Configure default cardinality limit for observable_counter instruments. If omitted or null, the
   * value from .default is used.
   */
  @JsonProperty("observable_counter")
  @JsonPropertyDescription(
      "Configure default cardinality limit for observable_counter instruments.\nIf omitted or null, the value from .default is used.\n")
  @Nullable
  private Integer observableCounter;

  /**
   * Configure default cardinality limit for observable_gauge instruments. If omitted or null, the
   * value from .default is used.
   */
  @JsonProperty("observable_gauge")
  @JsonPropertyDescription(
      "Configure default cardinality limit for observable_gauge instruments.\nIf omitted or null, the value from .default is used.\n")
  @Nullable
  private Integer observableGauge;

  /**
   * Configure default cardinality limit for observable_up_down_counter instruments. If omitted or
   * null, the value from .default is used.
   */
  @JsonProperty("observable_up_down_counter")
  @JsonPropertyDescription(
      "Configure default cardinality limit for observable_up_down_counter instruments.\nIf omitted or null, the value from .default is used.\n")
  @Nullable
  private Integer observableUpDownCounter;

  /**
   * Configure default cardinality limit for up_down_counter instruments. If omitted or null, the
   * value from .default is used.
   */
  @JsonProperty("up_down_counter")
  @JsonPropertyDescription(
      "Configure default cardinality limit for up_down_counter instruments.\nIf omitted or null, the value from .default is used.\n")
  @Nullable
  private Integer upDownCounter;

  /**
   * Configure default cardinality limit for all instrument types. Instrument-specific cardinality
   * limits take priority. If omitted or null, 2000 is used.
   */
  @JsonProperty("default")
  @Nullable
  public Integer getDefault() {
    return _default;
  }

  public CardinalityLimitsModel withDefault(Integer _default) {
    this._default = _default;
    return this;
  }

  /**
   * Configure default cardinality limit for counter instruments. If omitted or null, the value from
   * .default is used.
   */
  @JsonProperty("counter")
  @Nullable
  public Integer getCounter() {
    return counter;
  }

  public CardinalityLimitsModel withCounter(Integer counter) {
    this.counter = counter;
    return this;
  }

  /**
   * Configure default cardinality limit for gauge instruments. If omitted or null, the value from
   * .default is used.
   */
  @JsonProperty("gauge")
  @Nullable
  public Integer getGauge() {
    return gauge;
  }

  public CardinalityLimitsModel withGauge(Integer gauge) {
    this.gauge = gauge;
    return this;
  }

  /**
   * Configure default cardinality limit for histogram instruments. If omitted or null, the value
   * from .default is used.
   */
  @JsonProperty("histogram")
  @Nullable
  public Integer getHistogram() {
    return histogram;
  }

  public CardinalityLimitsModel withHistogram(Integer histogram) {
    this.histogram = histogram;
    return this;
  }

  /**
   * Configure default cardinality limit for observable_counter instruments. If omitted or null, the
   * value from .default is used.
   */
  @JsonProperty("observable_counter")
  @Nullable
  public Integer getObservableCounter() {
    return observableCounter;
  }

  public CardinalityLimitsModel withObservableCounter(Integer observableCounter) {
    this.observableCounter = observableCounter;
    return this;
  }

  /**
   * Configure default cardinality limit for observable_gauge instruments. If omitted or null, the
   * value from .default is used.
   */
  @JsonProperty("observable_gauge")
  @Nullable
  public Integer getObservableGauge() {
    return observableGauge;
  }

  public CardinalityLimitsModel withObservableGauge(Integer observableGauge) {
    this.observableGauge = observableGauge;
    return this;
  }

  /**
   * Configure default cardinality limit for observable_up_down_counter instruments. If omitted or
   * null, the value from .default is used.
   */
  @JsonProperty("observable_up_down_counter")
  @Nullable
  public Integer getObservableUpDownCounter() {
    return observableUpDownCounter;
  }

  public CardinalityLimitsModel withObservableUpDownCounter(Integer observableUpDownCounter) {
    this.observableUpDownCounter = observableUpDownCounter;
    return this;
  }

  /**
   * Configure default cardinality limit for up_down_counter instruments. If omitted or null, the
   * value from .default is used.
   */
  @JsonProperty("up_down_counter")
  @Nullable
  public Integer getUpDownCounter() {
    return upDownCounter;
  }

  public CardinalityLimitsModel withUpDownCounter(Integer upDownCounter) {
    this.upDownCounter = upDownCounter;
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
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this._default == null) ? 0 : this._default.hashCode();
    h *= 1000003;
    h ^= (this.counter == null) ? 0 : this.counter.hashCode();
    h *= 1000003;
    h ^= (this.gauge == null) ? 0 : this.gauge.hashCode();
    h *= 1000003;
    h ^= (this.histogram == null) ? 0 : this.histogram.hashCode();
    h *= 1000003;
    h ^= (this.observableCounter == null) ? 0 : this.observableCounter.hashCode();
    h *= 1000003;
    h ^= (this.observableGauge == null) ? 0 : this.observableGauge.hashCode();
    h *= 1000003;
    h ^= (this.observableUpDownCounter == null) ? 0 : this.observableUpDownCounter.hashCode();
    h *= 1000003;
    h ^= (this.upDownCounter == null) ? 0 : this.upDownCounter.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof CardinalityLimitsModel) {
      CardinalityLimitsModel that = (CardinalityLimitsModel) o;
      return (this._default == null ? that._default == null : this._default.equals(that._default))
          && (this.counter == null ? that.counter == null : this.counter.equals(that.counter))
          && (this.gauge == null ? that.gauge == null : this.gauge.equals(that.gauge))
          && (this.histogram == null
              ? that.histogram == null
              : this.histogram.equals(that.histogram))
          && (this.observableCounter == null
              ? that.observableCounter == null
              : this.observableCounter.equals(that.observableCounter))
          && (this.observableGauge == null
              ? that.observableGauge == null
              : this.observableGauge.equals(that.observableGauge))
          && (this.observableUpDownCounter == null
              ? that.observableUpDownCounter == null
              : this.observableUpDownCounter.equals(that.observableUpDownCounter))
          && (this.upDownCounter == null
              ? that.upDownCounter == null
              : this.upDownCounter.equals(that.upDownCounter));
    }
    return false;
  }
}
