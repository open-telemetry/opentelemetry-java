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
@JsonPropertyOrder({"ratio"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalProbabilitySamplerModel {

  /**
   * Configure ratio. If omitted or null, 1.0 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("ratio")
  @JsonPropertyDescription("Configure ratio.\nIf omitted or null, 1.0 is used.\n")
  private Double ratio;

  /** Configure ratio. If omitted or null, 1.0 is used. */
  @JsonProperty("ratio")
  @Nullable
  public Double getRatio() {
    return ratio;
  }

  public ExperimentalProbabilitySamplerModel withRatio(Double ratio) {
    this.ratio = ratio;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalProbabilitySamplerModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("ratio");
    sb.append('=');
    sb.append(((this.ratio == null) ? "<null>" : this.ratio));
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
    result = ((result * 31) + ((this.ratio == null) ? 0 : this.ratio.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalProbabilitySamplerModel) == false) {
      return false;
    }
    ExperimentalProbabilitySamplerModel rhs = ((ExperimentalProbabilitySamplerModel) other);
    return ((this.ratio == rhs.ratio) || ((this.ratio != null) && this.ratio.equals(rhs.ratio)));
  }
}
