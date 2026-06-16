/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "config"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalLoggerMatcherAndConfigModel {

  /**
   * Configure logger names to match, evaluated as follows:
   *
   * <p>* If the logger name exactly matches. * If the logger name matches the wildcard pattern,
   * where '?' matches any single character and '*' matches any number of characters including none.
   * Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("name")
  @JsonPropertyDescription(
      "Configure logger names to match, evaluated as follows:\n\n * If the logger name exactly matches.\n * If the logger name matches the wildcard pattern, where '?' matches any single character and '*' matches any number of characters including none.\nProperty is required and must be non-null.\n")
  @Nonnull
  private String name;

  /** (Required) */
  @JsonProperty("config")
  @Nonnull
  private ExperimentalLoggerConfigModel config;

  /**
   * Configure logger names to match, evaluated as follows:
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
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalLoggerMatcherAndConfigModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("name");
    sb.append('=');
    sb.append(((this.name == null) ? "<null>" : this.name));
    sb.append(',');
    sb.append("config");
    sb.append('=');
    sb.append(((this.config == null) ? "<null>" : this.config));
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
    result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
    result = ((result * 31) + ((this.config == null) ? 0 : this.config.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalLoggerMatcherAndConfigModel) == false) {
      return false;
    }
    ExperimentalLoggerMatcherAndConfigModel rhs = ((ExperimentalLoggerMatcherAndConfigModel) other);
    return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
        && ((this.config == rhs.config)
            || ((this.config != null) && this.config.equals(rhs.config))));
  }
}
