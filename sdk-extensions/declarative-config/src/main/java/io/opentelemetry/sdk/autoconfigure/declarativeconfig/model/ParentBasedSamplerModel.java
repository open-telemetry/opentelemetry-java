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
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ParentBasedSamplerModel {

  @Nullable private SamplerModel root;
  @Nullable private SamplerModel remoteParentSampled;
  @Nullable private SamplerModel remoteParentNotSampled;
  @Nullable private SamplerModel localParentSampled;
  @Nullable private SamplerModel localParentNotSampled;

  /**
   * Configure root sampler.
   *
   * <p>If omitted, always_on is used.
   */
  @JsonProperty("root")
  @Nullable
  public SamplerModel getRoot() {
    return root;
  }

  @JsonProperty("root")
  public ParentBasedSamplerModel withRoot(SamplerModel root) {
    this.root = root;
    return this;
  }

  /**
   * Configure remote_parent_sampled sampler.
   *
   * <p>If omitted, always_on is used.
   */
  @JsonProperty("remote_parent_sampled")
  @Nullable
  public SamplerModel getRemoteParentSampled() {
    return remoteParentSampled;
  }

  @JsonProperty("remote_parent_sampled")
  public ParentBasedSamplerModel withRemoteParentSampled(SamplerModel remoteParentSampled) {
    this.remoteParentSampled = remoteParentSampled;
    return this;
  }

  /**
   * Configure remote_parent_not_sampled sampler.
   *
   * <p>If omitted, always_off is used.
   */
  @JsonProperty("remote_parent_not_sampled")
  @Nullable
  public SamplerModel getRemoteParentNotSampled() {
    return remoteParentNotSampled;
  }

  @JsonProperty("remote_parent_not_sampled")
  public ParentBasedSamplerModel withRemoteParentNotSampled(SamplerModel remoteParentNotSampled) {
    this.remoteParentNotSampled = remoteParentNotSampled;
    return this;
  }

  /**
   * Configure local_parent_sampled sampler.
   *
   * <p>If omitted, always_on is used.
   */
  @JsonProperty("local_parent_sampled")
  @Nullable
  public SamplerModel getLocalParentSampled() {
    return localParentSampled;
  }

  @JsonProperty("local_parent_sampled")
  public ParentBasedSamplerModel withLocalParentSampled(SamplerModel localParentSampled) {
    this.localParentSampled = localParentSampled;
    return this;
  }

  /**
   * Configure local_parent_not_sampled sampler.
   *
   * <p>If omitted, always_off is used.
   */
  @JsonProperty("local_parent_not_sampled")
  @Nullable
  public SamplerModel getLocalParentNotSampled() {
    return localParentNotSampled;
  }

  @JsonProperty("local_parent_not_sampled")
  public ParentBasedSamplerModel withLocalParentNotSampled(SamplerModel localParentNotSampled) {
    this.localParentNotSampled = localParentNotSampled;
    return this;
  }

  @Override
  public String toString() {
    return "ParentBasedSamplerModel{"
        + "root="
        + root
        + ", remoteParentSampled="
        + remoteParentSampled
        + ", remoteParentNotSampled="
        + remoteParentNotSampled
        + ", localParentSampled="
        + localParentSampled
        + ", localParentNotSampled="
        + localParentNotSampled
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.root == null) ? 0 : this.root.hashCode();
    h *= 1000003;
    h ^= (this.remoteParentSampled == null) ? 0 : this.remoteParentSampled.hashCode();
    h *= 1000003;
    h ^= (this.remoteParentNotSampled == null) ? 0 : this.remoteParentNotSampled.hashCode();
    h *= 1000003;
    h ^= (this.localParentSampled == null) ? 0 : this.localParentSampled.hashCode();
    h *= 1000003;
    h ^= (this.localParentNotSampled == null) ? 0 : this.localParentNotSampled.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ParentBasedSamplerModel) {
      ParentBasedSamplerModel that = (ParentBasedSamplerModel) o;
      return (this.root == null ? that.root == null : this.root.equals(that.root))
          && (this.remoteParentSampled == null
              ? that.remoteParentSampled == null
              : this.remoteParentSampled.equals(that.remoteParentSampled))
          && (this.remoteParentNotSampled == null
              ? that.remoteParentNotSampled == null
              : this.remoteParentNotSampled.equals(that.remoteParentNotSampled))
          && (this.localParentSampled == null
              ? that.localParentSampled == null
              : this.localParentSampled.equals(that.localParentSampled))
          && (this.localParentNotSampled == null
              ? that.localParentNotSampled == null
              : this.localParentNotSampled.equals(that.localParentNotSampled));
    }
    return false;
  }
}
