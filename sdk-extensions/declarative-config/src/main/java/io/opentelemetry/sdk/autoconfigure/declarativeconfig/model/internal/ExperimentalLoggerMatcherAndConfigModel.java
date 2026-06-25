/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "config"})
@Generated("jsonschema2pojo")
public class ExperimentalLoggerMatcherAndConfigModel {

  /**
   * Configure logger names to match. Matching is case-sensitive, evaluated as follows:
   *
   * <p>* If the logger name exactly matches. * If the logger name matches the wildcard pattern,
   * where '?' matches any single character and '*' matches any number of characters including none.
   * Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("name")
  @JsonPropertyDescription(
      "Configure logger names to match. Matching is case-sensitive, evaluated as follows:\n\n * If the logger name exactly matches.\n * If the logger name matches the wildcard pattern, where '?' matches any single character and '*' matches any number of characters including none.\nProperty is required and must be non-null.\n")
  @Nullable
  private String name;

  /** (Required) */
  @JsonProperty("config")
  @Nullable
  private ExperimentalLoggerConfigModel config;

  /**
   * Configure logger names to match. Matching is case-sensitive, evaluated as follows:
   *
   * <p>* If the logger name exactly matches. * If the logger name matches the wildcard pattern,
   * where '?' matches any single character and '*' matches any number of characters including none.
   * Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("name")
  @Nullable
  public String getName() {
    return name;
  }

  public ExperimentalLoggerMatcherAndConfigModel withName(String name) {
    this.name = name;
    return this;
  }

  /** (Required) */
  @JsonProperty("config")
  @Nullable
  public ExperimentalLoggerConfigModel getConfig() {
    return config;
  }

  public ExperimentalLoggerMatcherAndConfigModel withConfig(ExperimentalLoggerConfigModel config) {
    this.config = config;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalLoggerMatcherAndConfigModel{" + "name=" + name + ", config=" + config + "}";
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
    if (o instanceof ExperimentalLoggerMatcherAndConfigModel) {
      ExperimentalLoggerMatcherAndConfigModel that = (ExperimentalLoggerMatcherAndConfigModel) o;
      return (this.name == null ? that.name == null : this.name.equals(that.name))
          && (this.config == null ? that.config == null : this.config.equals(that.config));
    }
    return false;
  }
}
