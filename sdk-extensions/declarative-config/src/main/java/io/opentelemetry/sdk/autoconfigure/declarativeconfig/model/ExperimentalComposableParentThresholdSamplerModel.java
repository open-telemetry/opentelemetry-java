/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"root"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalComposableParentThresholdSamplerModel {

  /** (Required) */
  @JsonProperty("root")
  @Nonnull
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
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalComposableParentThresholdSamplerModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("root");
    sb.append('=');
    sb.append(((this.root == null) ? "<null>" : this.root));
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
    result = ((result * 31) + ((this.root == null) ? 0 : this.root.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalComposableParentThresholdSamplerModel) == false) {
      return false;
    }
    ExperimentalComposableParentThresholdSamplerModel rhs =
        ((ExperimentalComposableParentThresholdSamplerModel) other);
    return ((this.root == rhs.root) || ((this.root != null) && this.root.equals(rhs.root)));
  }
}
