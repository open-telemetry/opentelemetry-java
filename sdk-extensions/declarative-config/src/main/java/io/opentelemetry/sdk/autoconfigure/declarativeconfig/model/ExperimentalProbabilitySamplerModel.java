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
public class ExperimentalProbabilitySamplerModel {

  /** Configure ratio. If omitted or null, 1.0 is used. */
  @JsonProperty("ratio")
  @JsonPropertyDescription("Configure ratio.\nIf omitted or null, 1.0 is used.\n")
  @Nullable
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
    return "ExperimentalProbabilitySamplerModel{" + "ratio=" + ratio + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.ratio == null) ? 0 : this.ratio.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalProbabilitySamplerModel) {
      ExperimentalProbabilitySamplerModel that = (ExperimentalProbabilitySamplerModel) o;
      return (this.ratio == null ? that.ratio == null : this.ratio.equals(that.ratio));
    }
    return false;
  }
}
