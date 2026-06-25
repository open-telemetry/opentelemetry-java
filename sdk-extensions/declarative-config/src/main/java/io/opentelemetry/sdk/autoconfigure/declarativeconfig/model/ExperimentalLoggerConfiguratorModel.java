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
public class ExperimentalLoggerConfiguratorModel {

  @JsonProperty("default_config")
  @Nullable
  private ExperimentalLoggerConfigModel defaultConfig;

  /** Configure loggers. If omitted, all loggers use .default_config. */
  @JsonProperty("loggers")
  @JsonPropertyDescription("Configure loggers.\nIf omitted, all loggers use .default_config.\n")
  @Nullable
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
    return "ExperimentalLoggerConfiguratorModel{"
        + "defaultConfig="
        + defaultConfig
        + ", loggers="
        + loggers
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.defaultConfig == null) ? 0 : this.defaultConfig.hashCode();
    h *= 1000003;
    h ^= (this.loggers == null) ? 0 : this.loggers.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalLoggerConfiguratorModel) {
      ExperimentalLoggerConfiguratorModel that = (ExperimentalLoggerConfiguratorModel) o;
      return (this.defaultConfig == null
              ? that.defaultConfig == null
              : this.defaultConfig.equals(that.defaultConfig))
          && (this.loggers == null ? that.loggers == null : this.loggers.equals(that.loggers));
    }
    return false;
  }
}
