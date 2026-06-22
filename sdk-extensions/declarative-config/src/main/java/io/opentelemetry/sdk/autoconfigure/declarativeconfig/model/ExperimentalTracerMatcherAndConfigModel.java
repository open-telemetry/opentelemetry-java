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
public class ExperimentalTracerMatcherAndConfigModel {

  /**
   * Configure tracer names to match. Matching is case-sensitive, evaluated as follows:
   *
   * <p>* If the tracer name exactly matches. * If the tracer name matches the wildcard pattern,
   * where '?' matches any single character and '*' matches any number of characters including none.
   * Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("name")
  @JsonPropertyDescription(
      "Configure tracer names to match. Matching is case-sensitive, evaluated as follows:\n\n * If the tracer name exactly matches.\n * If the tracer name matches the wildcard pattern, where '?' matches any single character and '*' matches any number of characters including none.\nProperty is required and must be non-null.\n")
  @Nonnull
  private String name;

  /** (Required) */
  @JsonProperty("config")
  @Nonnull
  private ExperimentalTracerConfigModel config;

  /**
   * Configure tracer names to match. Matching is case-sensitive, evaluated as follows:
   *
   * <p>* If the tracer name exactly matches. * If the tracer name matches the wildcard pattern,
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

  public ExperimentalTracerMatcherAndConfigModel withName(String name) {
    this.name = name;
    return this;
  }

  /** (Required) */
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
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalTracerMatcherAndConfigModel.class.getName())
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
    if ((other instanceof ExperimentalTracerMatcherAndConfigModel) == false) {
      return false;
    }
    ExperimentalTracerMatcherAndConfigModel rhs = ((ExperimentalTracerMatcherAndConfigModel) other);
    return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
        && ((this.config == rhs.config)
            || ((this.config != null) && this.config.equals(rhs.config))));
  }
}
