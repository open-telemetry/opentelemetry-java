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
public class ExperimentalTracerMatcherAndConfigModel {

  @JsonProperty("name")
  @Nullable
  private String name;

  @JsonProperty("config")
  @Nullable
  private ExperimentalTracerConfigModel config;

  /**
   * Configure tracer names to match. Matching is case-sensitive, evaluated as follows:
   *
   * <p>* If the tracer name exactly matches.
   *
   * <p>* If the tracer name matches the wildcard pattern, where '?' matches any single character
   * and '*' matches any number of characters including none.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("name")
  @Nullable
  public String getName() {
    return name;
  }

  public ExperimentalTracerMatcherAndConfigModel withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * The tracer config.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("config")
  @Nullable
  public ExperimentalTracerConfigModel getConfig() {
    return config;
  }

  public ExperimentalTracerMatcherAndConfigModel withConfig(ExperimentalTracerConfigModel config) {
    this.config = config;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalTracerMatcherAndConfigModel{" + "name=" + name + ", config=" + config + "}";
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
    if (o instanceof ExperimentalTracerMatcherAndConfigModel) {
      ExperimentalTracerMatcherAndConfigModel that = (ExperimentalTracerMatcherAndConfigModel) o;
      return (this.name == null ? that.name == null : this.name.equals(that.name))
          && (this.config == null ? that.config == null : this.config.equals(that.config));
    }
    return false;
  }
}
