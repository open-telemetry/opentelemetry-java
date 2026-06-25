/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "config"})
@Generated("jsonschema2pojo")
public class ExperimentalMeterMatcherAndConfigModel {

  @JsonProperty("name")
  @Nullable
  private String name;

  @JsonProperty("config")
  @Nullable
  private ExperimentalMeterConfigModel config;

  /**
   * Configure meter names to match. Matching is case-sensitive, evaluated as follows:
   *
   * <p>* If the meter name exactly matches.
   *
   * <p>* If the meter name matches the wildcard pattern, where '?' matches any single character and
   * '*' matches any number of characters including none.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("name")
  @Nullable
  public String getName() {
    return name;
  }

  public ExperimentalMeterMatcherAndConfigModel withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * The meter config.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("config")
  @Nullable
  public ExperimentalMeterConfigModel getConfig() {
    return config;
  }

  public ExperimentalMeterMatcherAndConfigModel withConfig(ExperimentalMeterConfigModel config) {
    this.config = config;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalMeterMatcherAndConfigModel{" + "name=" + name + ", config=" + config + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.name == null) ? 0 : this.name.hashCode();
    h *= 1000003;
    h ^= (this.config == null) ? 0 : this.config.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalMeterMatcherAndConfigModel) {
      ExperimentalMeterMatcherAndConfigModel that = (ExperimentalMeterMatcherAndConfigModel) o;
      return (this.name == null ? that.name == null : this.name.equals(that.name))
          && (this.config == null ? that.config == null : this.config.equals(that.config));
    }
    return false;
  }
}
