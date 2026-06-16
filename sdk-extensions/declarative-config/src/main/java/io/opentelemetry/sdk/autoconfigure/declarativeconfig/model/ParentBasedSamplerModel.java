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
@JsonPropertyOrder({
  "root",
  "remote_parent_sampled",
  "remote_parent_not_sampled",
  "local_parent_sampled",
  "local_parent_not_sampled"
})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ParentBasedSamplerModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("root")
  private SamplerModel root;

  /** (Can be null) */
  @Nullable
  @JsonProperty("remote_parent_sampled")
  private SamplerModel remoteParentSampled;

  /** (Can be null) */
  @Nullable
  @JsonProperty("remote_parent_not_sampled")
  private SamplerModel remoteParentNotSampled;

  /** (Can be null) */
  @Nullable
  @JsonProperty("local_parent_sampled")
  private SamplerModel localParentSampled;

  /** (Can be null) */
  @Nullable
  @JsonProperty("local_parent_not_sampled")
  private SamplerModel localParentNotSampled;

  @JsonProperty("root")
  @Nullable
  public SamplerModel getRoot() {
    return root;
  }

  public ParentBasedSamplerModel withRoot(SamplerModel root) {
    this.root = root;
    return this;
  }

  @JsonProperty("remote_parent_sampled")
  @Nullable
  public SamplerModel getRemoteParentSampled() {
    return remoteParentSampled;
  }

  public ParentBasedSamplerModel withRemoteParentSampled(SamplerModel remoteParentSampled) {
    this.remoteParentSampled = remoteParentSampled;
    return this;
  }

  @JsonProperty("remote_parent_not_sampled")
  @Nullable
  public SamplerModel getRemoteParentNotSampled() {
    return remoteParentNotSampled;
  }

  public ParentBasedSamplerModel withRemoteParentNotSampled(SamplerModel remoteParentNotSampled) {
    this.remoteParentNotSampled = remoteParentNotSampled;
    return this;
  }

  @JsonProperty("local_parent_sampled")
  @Nullable
  public SamplerModel getLocalParentSampled() {
    return localParentSampled;
  }

  public ParentBasedSamplerModel withLocalParentSampled(SamplerModel localParentSampled) {
    this.localParentSampled = localParentSampled;
    return this;
  }

  @JsonProperty("local_parent_not_sampled")
  @Nullable
  public SamplerModel getLocalParentNotSampled() {
    return localParentNotSampled;
  }

  public ParentBasedSamplerModel withLocalParentNotSampled(SamplerModel localParentNotSampled) {
    this.localParentNotSampled = localParentNotSampled;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ParentBasedSamplerModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("root");
    sb.append('=');
    sb.append(((this.root == null) ? "<null>" : this.root));
    sb.append(',');
    sb.append("remoteParentSampled");
    sb.append('=');
    sb.append(((this.remoteParentSampled == null) ? "<null>" : this.remoteParentSampled));
    sb.append(',');
    sb.append("remoteParentNotSampled");
    sb.append('=');
    sb.append(((this.remoteParentNotSampled == null) ? "<null>" : this.remoteParentNotSampled));
    sb.append(',');
    sb.append("localParentSampled");
    sb.append('=');
    sb.append(((this.localParentSampled == null) ? "<null>" : this.localParentSampled));
    sb.append(',');
    sb.append("localParentNotSampled");
    sb.append('=');
    sb.append(((this.localParentNotSampled == null) ? "<null>" : this.localParentNotSampled));
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
    result =
        ((result * 31)
            + ((this.remoteParentNotSampled == null) ? 0 : this.remoteParentNotSampled.hashCode()));
    result =
        ((result * 31)
            + ((this.localParentNotSampled == null) ? 0 : this.localParentNotSampled.hashCode()));
    result =
        ((result * 31)
            + ((this.remoteParentSampled == null) ? 0 : this.remoteParentSampled.hashCode()));
    result = ((result * 31) + ((this.root == null) ? 0 : this.root.hashCode()));
    result =
        ((result * 31)
            + ((this.localParentSampled == null) ? 0 : this.localParentSampled.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ParentBasedSamplerModel) == false) {
      return false;
    }
    ParentBasedSamplerModel rhs = ((ParentBasedSamplerModel) other);
    return ((((((this.remoteParentNotSampled == rhs.remoteParentNotSampled)
                        || ((this.remoteParentNotSampled != null)
                            && this.remoteParentNotSampled.equals(rhs.remoteParentNotSampled)))
                    && ((this.localParentNotSampled == rhs.localParentNotSampled)
                        || ((this.localParentNotSampled != null)
                            && this.localParentNotSampled.equals(rhs.localParentNotSampled))))
                && ((this.remoteParentSampled == rhs.remoteParentSampled)
                    || ((this.remoteParentSampled != null)
                        && this.remoteParentSampled.equals(rhs.remoteParentSampled))))
            && ((this.root == rhs.root) || ((this.root != null) && this.root.equals(rhs.root))))
        && ((this.localParentSampled == rhs.localParentSampled)
            || ((this.localParentSampled != null)
                && this.localParentSampled.equals(rhs.localParentSampled))));
  }
}
