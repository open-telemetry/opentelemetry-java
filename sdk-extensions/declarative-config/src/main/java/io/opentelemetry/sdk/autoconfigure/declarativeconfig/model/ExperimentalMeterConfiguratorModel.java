/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"default_config", "meters"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalMeterConfiguratorModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("default_config")
  private ExperimentalMeterConfigModel defaultConfig;

  /**
   * Configure meters. If omitted, all meters used .default_config.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("meters")
  @JsonPropertyDescription("Configure meters.\nIf omitted, all meters used .default_config.\n")
  private List<ExperimentalMeterMatcherAndConfigModel> meters;

  @JsonProperty("default_config")
  @Nullable
  public ExperimentalMeterConfigModel getDefaultConfig() {
    return defaultConfig;
  }

  public ExperimentalMeterConfiguratorModel withDefaultConfig(
      ExperimentalMeterConfigModel defaultConfig) {
    this.defaultConfig = defaultConfig;
    return this;
  }

  /** Configure meters. If omitted, all meters used .default_config. */
  @JsonProperty("meters")
  @Nullable
  public List<ExperimentalMeterMatcherAndConfigModel> getMeters() {
    return meters;
  }

  public ExperimentalMeterConfiguratorModel withMeters(
      List<ExperimentalMeterMatcherAndConfigModel> meters) {
    this.meters = meters;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalMeterConfiguratorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("defaultConfig");
    sb.append('=');
    sb.append(((this.defaultConfig == null) ? "<null>" : this.defaultConfig));
    sb.append(',');
    sb.append("meters");
    sb.append('=');
    sb.append(((this.meters == null) ? "<null>" : this.meters));
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
    result = ((result * 31) + ((this.defaultConfig == null) ? 0 : this.defaultConfig.hashCode()));
    result = ((result * 31) + ((this.meters == null) ? 0 : this.meters.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalMeterConfiguratorModel) == false) {
      return false;
    }
    ExperimentalMeterConfiguratorModel rhs = ((ExperimentalMeterConfiguratorModel) other);
    return (((this.defaultConfig == rhs.defaultConfig)
            || ((this.defaultConfig != null) && this.defaultConfig.equals(rhs.defaultConfig)))
        && ((this.meters == rhs.meters)
            || ((this.meters != null) && this.meters.equals(rhs.meters))));
  }
}
