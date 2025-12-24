/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import java.util.logging.Logger;
import javax.annotation.Nullable;

/** A builder for creating ParentBased sampler instances. */
public final class ParentBasedSamplerBuilder {

  private static final Logger logger = Logger.getLogger(ParentBasedSamplerBuilder.class.getName());

  private final Sampler root;
  @Nullable private Sampler remoteParentSampled;
  @Nullable private Sampler remoteParentNotSampled;
  @Nullable private Sampler localParentSampled;
  @Nullable private Sampler localParentNotSampled;
  private boolean warnedRemoteParentSampled;
  private boolean warnedRemoteParentNotSampled;
  private boolean warnedLocalParentSampled;
  private boolean warnedLocalParentNotSampled;

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
    if (!warnedRemoteParentSampled && remoteParentSampled instanceof TraceIdRatioBasedSampler) {
      warnedRemoteParentSampled = true;
      logger.warning(
          "TraceIdRatioBasedSampler is being used as a child sampler (remoteParentSampled). "
              + "This configuration is discouraged per the OpenTelemetry specification "
              + "and may lead to unexpected sampling behavior.");
    }
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
    if (!warnedRemoteParentNotSampled
        && remoteParentNotSampled instanceof TraceIdRatioBasedSampler) {
      warnedRemoteParentNotSampled = true;
      logger.warning(
          "TraceIdRatioBasedSampler is being used as a child sampler (remoteParentNotSampled). "
              + "This configuration is discouraged per the OpenTelemetry specification "
              + "and may lead to unexpected sampling behavior.");
    }
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
    if (!warnedLocalParentSampled && localParentSampled instanceof TraceIdRatioBasedSampler) {
      warnedLocalParentSampled = true;
      logger.warning(
          "TraceIdRatioBasedSampler is being used as a child sampler (localParentSampled). "
              + "This configuration is discouraged per the OpenTelemetry specification "
              + "and may lead to unexpected sampling behavior.");
    }
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
    if (!warnedLocalParentNotSampled && localParentNotSampled instanceof TraceIdRatioBasedSampler) {
      warnedLocalParentNotSampled = true;
      logger.warning(
          "TraceIdRatioBasedSampler is being used as a child sampler (localParentNotSampled). "
              + "This configuration is discouraged per the OpenTelemetry specification "
              + "and may lead to unexpected sampling behavior.");
    }
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
