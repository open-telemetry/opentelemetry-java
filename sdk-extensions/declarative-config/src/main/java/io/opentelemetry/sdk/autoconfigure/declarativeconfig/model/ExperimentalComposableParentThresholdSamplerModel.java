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
@JsonPropertyOrder({"root"})
@Generated("jsonschema2pojo")
public class ExperimentalComposableParentThresholdSamplerModel {

  /** (Required) */
  @JsonProperty("root")
  @Nullable
  private ExperimentalComposableSamplerModel root;

  /** (Required) */
  @JsonProperty("root")
  @Nullable
  public ExperimentalComposableSamplerModel getRoot() {
    return root;
  }

  public ExperimentalComposableParentThresholdSamplerModel withRoot(
      ExperimentalComposableSamplerModel root) {
    this.root = root;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalComposableParentThresholdSamplerModel{" + "root=" + root + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.root == null) ? 0 : this.root.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalComposableParentThresholdSamplerModel) {
      ExperimentalComposableParentThresholdSamplerModel that =
          (ExperimentalComposableParentThresholdSamplerModel) o;
      return (this.root == null ? that.root == null : this.root.equals(that.root));
    }
    return false;
  }
}
