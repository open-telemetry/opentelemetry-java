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
@JsonPropertyOrder({"default_config", "tracers"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalTracerConfiguratorModel {

  @Nullable private ExperimentalTracerConfigModel defaultConfig;
  @Nullable private List<ExperimentalTracerMatcherAndConfigModel> tracers;

  /**
   * Configure the default tracer config used there is no matching entry in
   * .tracer_configurator/development.tracers.
   *
   * <p>If omitted, unmatched .tracers use default values as described in ExperimentalTracerConfig.
   */
  @JsonProperty("default_config")
  @Nullable
  public ExperimentalTracerConfigModel getDefaultConfig() {
    return defaultConfig;
  }

  @JsonProperty("default_config")
  public ExperimentalTracerConfiguratorModel withDefaultConfig(
      ExperimentalTracerConfigModel defaultConfig) {
    this.defaultConfig = defaultConfig;
    return this;
  }

  /**
   * Configure tracers.
   *
   * <p>If omitted, all tracers use .default_config.
   */
  @JsonProperty("tracers")
  @Nullable
  public List<ExperimentalTracerMatcherAndConfigModel> getTracers() {
    return tracers;
  }

  @JsonProperty("tracers")
  public ExperimentalTracerConfiguratorModel withTracers(
      List<ExperimentalTracerMatcherAndConfigModel> tracers) {
    this.tracers = tracers;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalTracerConfiguratorModel{"
        + "defaultConfig="
        + defaultConfig
        + ", tracers="
        + tracers
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.defaultConfig == null) ? 0 : this.defaultConfig.hashCode();
    h *= 1000003;
    h ^= (this.tracers == null) ? 0 : this.tracers.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalTracerConfiguratorModel) {
      ExperimentalTracerConfiguratorModel that = (ExperimentalTracerConfiguratorModel) o;
      return (this.defaultConfig == null
              ? that.defaultConfig == null
              : this.defaultConfig.equals(that.defaultConfig))
          && (this.tracers == null ? that.tracers == null : this.tracers.equals(that.tracers));
    }
    return false;
  }
}
