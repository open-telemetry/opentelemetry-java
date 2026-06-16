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
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"enabled"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalTracerConfigModel {

  /**
   * Configure if the tracer is enabled or not. If omitted, true is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("enabled")
  @JsonPropertyDescription(
      "Configure if the tracer is enabled or not.\nIf omitted, true is used.\n")
  private Boolean enabled;

  /** Configure if the tracer is enabled or not. If omitted, true is used. */
  @JsonProperty("enabled")
  @Nullable
  public Boolean getEnabled() {
    return enabled;
  }

  public ExperimentalTracerConfigModel withEnabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalTracerConfigModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("enabled");
    sb.append('=');
    sb.append(((this.enabled == null) ? "<null>" : this.enabled));
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
    result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalTracerConfigModel) == false) {
      return false;
    }
    ExperimentalTracerConfigModel rhs = ((ExperimentalTracerConfigModel) other);
    return ((this.enabled == rhs.enabled)
        || ((this.enabled != null) && this.enabled.equals(rhs.enabled)));
  }
}
