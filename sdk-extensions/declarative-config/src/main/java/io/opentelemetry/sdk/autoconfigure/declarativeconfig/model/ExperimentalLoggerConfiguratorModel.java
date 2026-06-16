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
@JsonPropertyOrder({"default_config", "loggers"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalLoggerConfiguratorModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("default_config")
  private ExperimentalLoggerConfigModel defaultConfig;

  /**
   * Configure loggers. If omitted, all loggers use .default_config.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("loggers")
  @JsonPropertyDescription("Configure loggers.\nIf omitted, all loggers use .default_config.\n")
  private List<ExperimentalLoggerMatcherAndConfigModel> loggers;

  @JsonProperty("default_config")
  @Nullable
  public ExperimentalLoggerConfigModel getDefaultConfig() {
    return defaultConfig;
  }

  public ExperimentalLoggerConfiguratorModel withDefaultConfig(
      ExperimentalLoggerConfigModel defaultConfig) {
    this.defaultConfig = defaultConfig;
    return this;
  }

  /** Configure loggers. If omitted, all loggers use .default_config. */
  @JsonProperty("loggers")
  @Nullable
  public List<ExperimentalLoggerMatcherAndConfigModel> getLoggers() {
    return loggers;
  }

  public ExperimentalLoggerConfiguratorModel withLoggers(
      List<ExperimentalLoggerMatcherAndConfigModel> loggers) {
    this.loggers = loggers;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalLoggerConfiguratorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("defaultConfig");
    sb.append('=');
    sb.append(((this.defaultConfig == null) ? "<null>" : this.defaultConfig));
    sb.append(',');
    sb.append("loggers");
    sb.append('=');
    sb.append(((this.loggers == null) ? "<null>" : this.loggers));
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
    result = ((result * 31) + ((this.loggers == null) ? 0 : this.loggers.hashCode()));
    result = ((result * 31) + ((this.defaultConfig == null) ? 0 : this.defaultConfig.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalLoggerConfiguratorModel) == false) {
      return false;
    }
    ExperimentalLoggerConfiguratorModel rhs = ((ExperimentalLoggerConfiguratorModel) other);
    return (((this.loggers == rhs.loggers)
            || ((this.loggers != null) && this.loggers.equals(rhs.loggers)))
        && ((this.defaultConfig == rhs.defaultConfig)
            || ((this.defaultConfig != null) && this.defaultConfig.equals(rhs.defaultConfig))));
  }
}
