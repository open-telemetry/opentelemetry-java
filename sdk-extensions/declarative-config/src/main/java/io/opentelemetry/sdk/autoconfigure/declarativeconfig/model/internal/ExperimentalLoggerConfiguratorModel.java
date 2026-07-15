/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"default_config", "loggers"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalLoggerConfiguratorModel {

  @Nullable private ExperimentalLoggerConfigModel defaultConfig;
  @Nullable private List<ExperimentalLoggerMatcherAndConfigModel> loggers;

  /**
   * Configure the default logger config used there is no matching entry in
   * .logger_configurator/development.loggers.
   *
   * <p>If omitted, unmatched .loggers use default values as described in ExperimentalLoggerConfig.
   */
  @JsonProperty("default_config")
  @Nullable
  public ExperimentalLoggerConfigModel getDefaultConfig() {
    return defaultConfig;
  }

  @JsonProperty("default_config")
  public ExperimentalLoggerConfiguratorModel withDefaultConfig(
      ExperimentalLoggerConfigModel defaultConfig) {
    this.defaultConfig = defaultConfig;
    return this;
  }

  /**
   * Configure loggers.
   *
   * <p>If omitted, all loggers use .default_config.
   */
  @JsonProperty("loggers")
  @Nullable
  public List<ExperimentalLoggerMatcherAndConfigModel> getLoggers() {
    return loggers;
  }

  @JsonProperty("loggers")
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
