/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

/** A builder for creating ParentBased sampler instances. */
public final class ParentBasedSamplerBuilder {

  private final Sampler root;
  private Sampler remoteParentSampled;
  private Sampler remoteParentNotSampled;
  private Sampler localParentSampled;
  private Sampler localParentNotSampled;

  ParentBasedSamplerBuilder(Sampler root) {
    this.root = root;
  }

  /**
   * Sets the {@link Sampler} to use when there is a remote parent that was sampled. If not set,
   * defaults to always sampling if the remote parent was sampled.
   *
   * @return this Builder
   */
  public ParentBasedSamplerBuilder setRemoteParentSampled(Sampler remoteParentSampled) {
    this.remoteParentSampled = remoteParentSampled;
    return this;
  }

  /**
   * Sets the {@link Sampler} to use when there is a remote parent that was not sampled. If not set,
   * defaults to never sampling when the remote parent isn't sampled.
   *
   * @return this Builder
   */
  public ParentBasedSamplerBuilder setRemoteParentNotSampled(Sampler remoteParentNotSampled) {
    this.remoteParentNotSampled = remoteParentNotSampled;
    return this;
  }

  /**
   * Sets the {@link Sampler} to use when there is a local parent that was sampled. If not set,
   * defaults to always sampling if the local parent was sampled.
   *
   * @return this Builder
   */
  public ParentBasedSamplerBuilder setLocalParentSampled(Sampler localParentSampled) {
    this.localParentSampled = localParentSampled;
    return this;
  }

  /**
   * Sets the {@link Sampler} to use when there is a local parent that was not sampled. If not set,
   * defaults to never sampling when the local parent isn't sampled.
   *
   * @return this Builder
   */
  public ParentBasedSamplerBuilder setLocalParentNotSampled(Sampler localParentNotSampled) {
    this.localParentNotSampled = localParentNotSampled;
    return this;
  }

  /**
   * Builds the {@link ParentBasedSampler}.
   *
   * @return the ParentBased sampler.
   */
  public Sampler build() {
    return new ParentBasedSampler(
        this.root,
        this.remoteParentSampled,
        this.remoteParentNotSampled,
        this.localParentSampled,
        this.localParentNotSampled);
  }
}
