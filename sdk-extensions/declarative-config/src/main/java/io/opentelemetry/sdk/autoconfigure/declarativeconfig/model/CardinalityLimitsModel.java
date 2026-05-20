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
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class CardinalityLimitsModel {

  /**
   * Configure default cardinality limit for all instrument types. Instrument-specific cardinality
   * limits take priority. If omitted or null, 2000 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("default")
  @JsonPropertyDescription(
      "Configure default cardinality limit for all instrument types.\nInstrument-specific cardinality limits take priority.\nIf omitted or null, 2000 is used.\n")
  private Integer _default;

  /**
   * Configure default cardinality limit for counter instruments. If omitted or null, the value from
   * .default is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("counter")
  @JsonPropertyDescription(
      "Configure default cardinality limit for counter instruments.\nIf omitted or null, the value from .default is used.\n")
  private Integer counter;

  /**
   * Configure default cardinality limit for gauge instruments. If omitted or null, the value from
   * .default is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("gauge")
  @JsonPropertyDescription(
      "Configure default cardinality limit for gauge instruments.\nIf omitted or null, the value from .default is used.\n")
  private Integer gauge;

  /**
   * Configure default cardinality limit for histogram instruments. If omitted or null, the value
   * from .default is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("histogram")
  @JsonPropertyDescription(
      "Configure default cardinality limit for histogram instruments.\nIf omitted or null, the value from .default is used.\n")
  private Integer histogram;

  /**
   * Configure default cardinality limit for observable_counter instruments. If omitted or null, the
   * value from .default is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("observable_counter")
  @JsonPropertyDescription(
      "Configure default cardinality limit for observable_counter instruments.\nIf omitted or null, the value from .default is used.\n")
  private Integer observableCounter;

  /**
   * Configure default cardinality limit for observable_gauge instruments. If omitted or null, the
   * value from .default is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("observable_gauge")
  @JsonPropertyDescription(
      "Configure default cardinality limit for observable_gauge instruments.\nIf omitted or null, the value from .default is used.\n")
  private Integer observableGauge;

  /**
   * Configure default cardinality limit for observable_up_down_counter instruments. If omitted or
   * null, the value from .default is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("observable_up_down_counter")
  @JsonPropertyDescription(
      "Configure default cardinality limit for observable_up_down_counter instruments.\nIf omitted or null, the value from .default is used.\n")
  private Integer observableUpDownCounter;

  /**
   * Configure default cardinality limit for up_down_counter instruments. If omitted or null, the
   * value from .default is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("up_down_counter")
  @JsonPropertyDescription(
      "Configure default cardinality limit for up_down_counter instruments.\nIf omitted or null, the value from .default is used.\n")
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
    StringBuilder sb = new StringBuilder();
    sb.append(CardinalityLimitsModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("_default");
    sb.append('=');
    sb.append(((this._default == null) ? "<null>" : this._default));
    sb.append(',');
    sb.append("counter");
    sb.append('=');
    sb.append(((this.counter == null) ? "<null>" : this.counter));
    sb.append(',');
    sb.append("gauge");
    sb.append('=');
    sb.append(((this.gauge == null) ? "<null>" : this.gauge));
    sb.append(',');
    sb.append("histogram");
    sb.append('=');
    sb.append(((this.histogram == null) ? "<null>" : this.histogram));
    sb.append(',');
    sb.append("observableCounter");
    sb.append('=');
    sb.append(((this.observableCounter == null) ? "<null>" : this.observableCounter));
    sb.append(',');
    sb.append("observableGauge");
    sb.append('=');
    sb.append(((this.observableGauge == null) ? "<null>" : this.observableGauge));
    sb.append(',');
    sb.append("observableUpDownCounter");
    sb.append('=');
    sb.append(((this.observableUpDownCounter == null) ? "<null>" : this.observableUpDownCounter));
    sb.append(',');
    sb.append("upDownCounter");
    sb.append('=');
    sb.append(((this.upDownCounter == null) ? "<null>" : this.upDownCounter));
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
    result = ((result * 31) + ((this.gauge == null) ? 0 : this.gauge.hashCode()));
    result = ((result * 31) + ((this.histogram == null) ? 0 : this.histogram.hashCode()));
    result = ((result * 31) + ((this._default == null) ? 0 : this._default.hashCode()));
    result =
        ((result * 31) + ((this.observableGauge == null) ? 0 : this.observableGauge.hashCode()));
    result = ((result * 31) + ((this.counter == null) ? 0 : this.counter.hashCode()));
    result =
        ((result * 31)
            + ((this.observableUpDownCounter == null)
                ? 0
                : this.observableUpDownCounter.hashCode()));
    result =
        ((result * 31)
            + ((this.observableCounter == null) ? 0 : this.observableCounter.hashCode()));
    result = ((result * 31) + ((this.upDownCounter == null) ? 0 : this.upDownCounter.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof CardinalityLimitsModel) == false) {
      return false;
    }
    CardinalityLimitsModel rhs = ((CardinalityLimitsModel) other);
    return (((((((((this.gauge == rhs.gauge)
                                    || ((this.gauge != null) && this.gauge.equals(rhs.gauge)))
                                && ((this.histogram == rhs.histogram)
                                    || ((this.histogram != null)
                                        && this.histogram.equals(rhs.histogram))))
                            && ((this._default == rhs._default)
                                || ((this._default != null) && this._default.equals(rhs._default))))
                        && ((this.observableGauge == rhs.observableGauge)
                            || ((this.observableGauge != null)
                                && this.observableGauge.equals(rhs.observableGauge))))
                    && ((this.counter == rhs.counter)
                        || ((this.counter != null) && this.counter.equals(rhs.counter))))
                && ((this.observableUpDownCounter == rhs.observableUpDownCounter)
                    || ((this.observableUpDownCounter != null)
                        && this.observableUpDownCounter.equals(rhs.observableUpDownCounter))))
            && ((this.observableCounter == rhs.observableCounter)
                || ((this.observableCounter != null)
                    && this.observableCounter.equals(rhs.observableCounter))))
        && ((this.upDownCounter == rhs.upDownCounter)
            || ((this.upDownCounter != null) && this.upDownCounter.equals(rhs.upDownCounter))));
  }
}
