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
public class ExperimentalMeterConfiguratorModel {

  @JsonProperty("default_config")
  @Nullable
  private ExperimentalMeterConfigModel defaultConfig;

  /** Configure meters. If omitted, all meters used .default_config. */
  @JsonProperty("meters")
  @JsonPropertyDescription("Configure meters.\nIf omitted, all meters used .default_config.\n")
  @Nullable
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
    return "ExperimentalMeterConfiguratorModel{"
        + "defaultConfig="
        + defaultConfig
        + ", meters="
        + meters
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.defaultConfig == null) ? 0 : this.defaultConfig.hashCode();
    h *= 1000003;
    h ^= (this.meters == null) ? 0 : this.meters.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalMeterConfiguratorModel) {
      ExperimentalMeterConfiguratorModel that = (ExperimentalMeterConfiguratorModel) o;
      return (this.defaultConfig == null
              ? that.defaultConfig == null
              : this.defaultConfig.equals(that.defaultConfig))
          && (this.meters == null ? that.meters == null : this.meters.equals(that.meters));
    }
    return false;
  }
}
