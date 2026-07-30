/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"ratio"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalComposableProbabilitySamplerModel {

  @Nullable private Double ratio;

  /**
   * Configure ratio.
   *
   * <p>If omitted or null, 1.0 is used.
   */
  @JsonProperty("ratio")
  @Nullable
  public Double getRatio() {
    return ratio;
  }

  @JsonProperty("ratio")
  public ExperimentalComposableProbabilitySamplerModel withRatio(Double ratio) {
    this.ratio = ratio;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalComposableProbabilitySamplerModel{" + "ratio=" + ratio + "}";
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
    if (o instanceof ExperimentalComposableProbabilitySamplerModel) {
      ExperimentalComposableProbabilitySamplerModel that =
          (ExperimentalComposableProbabilitySamplerModel) o;
      return (this.ratio == null ? that.ratio == null : this.ratio.equals(that.ratio));
    }
    return false;
  }
}
