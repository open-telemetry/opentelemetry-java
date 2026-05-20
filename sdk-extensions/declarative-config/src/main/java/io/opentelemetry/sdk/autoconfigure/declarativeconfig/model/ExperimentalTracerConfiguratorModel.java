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
@JsonPropertyOrder({"default_config", "tracers"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalTracerConfiguratorModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("default_config")
  private ExperimentalTracerConfigModel defaultConfig;

  /**
   * Configure tracers. If omitted, all tracers use .default_config.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("tracers")
  @JsonPropertyDescription("Configure tracers.\nIf omitted, all tracers use .default_config.\n")
  private List<ExperimentalTracerMatcherAndConfigModel> tracers;

  @JsonProperty("default_config")
  @Nullable
  public ExperimentalTracerConfigModel getDefaultConfig() {
    return defaultConfig;
  }

  public ExperimentalTracerConfiguratorModel withDefaultConfig(
      ExperimentalTracerConfigModel defaultConfig) {
    this.defaultConfig = defaultConfig;
    return this;
  }

  /** Configure tracers. If omitted, all tracers use .default_config. */
  @JsonProperty("tracers")
  @Nullable
  public List<ExperimentalTracerMatcherAndConfigModel> getTracers() {
    return tracers;
  }

  public ExperimentalTracerConfiguratorModel withTracers(
      List<ExperimentalTracerMatcherAndConfigModel> tracers) {
    this.tracers = tracers;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalTracerConfiguratorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("defaultConfig");
    sb.append('=');
    sb.append(((this.defaultConfig == null) ? "<null>" : this.defaultConfig));
    sb.append(',');
    sb.append("tracers");
    sb.append('=');
    sb.append(((this.tracers == null) ? "<null>" : this.tracers));
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
    result = ((result * 31) + ((this.tracers == null) ? 0 : this.tracers.hashCode()));
    result = ((result * 31) + ((this.defaultConfig == null) ? 0 : this.defaultConfig.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalTracerConfiguratorModel) == false) {
      return false;
    }
    ExperimentalTracerConfiguratorModel rhs = ((ExperimentalTracerConfiguratorModel) other);
    return (((this.tracers == rhs.tracers)
            || ((this.tracers != null) && this.tracers.equals(rhs.tracers)))
        && ((this.defaultConfig == rhs.defaultConfig)
            || ((this.defaultConfig != null) && this.defaultConfig.equals(rhs.defaultConfig))));
  }
}
