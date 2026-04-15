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

  ParentBasedSamplerBuilder(Sampler root) {
    // This is the only valid traceIdSampler location according to Spec
    // https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/sdk.md#compatibility-warnings-for-traceidratiobased-sampler
    this.root = root;
  }

  /**
   * Sets the {@link Sampler} to use when there is a remote parent that was sampled. If not set,
   * defaults to always sampling if the remote parent was sampled.
   *
   * @return this Builder
   */
  public ParentBasedSamplerBuilder setRemoteParentSampled(Sampler remoteParentSampled) {
    maybeLogTraceIdSamplerWarning(remoteParentSampled, "remoteParentSampled");
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
    maybeLogTraceIdSamplerWarning(remoteParentNotSampled, "remoteParentNotSampled");
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
    maybeLogTraceIdSamplerWarning(localParentSampled, "localParentSampled");
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
    maybeLogTraceIdSamplerWarning(localParentNotSampled, "localParentNotSampled");
    this.localParentNotSampled = localParentNotSampled;
    return this;
  }

  private static void maybeLogTraceIdSamplerWarning(Sampler sampler, String field) {
    if (sampler instanceof TraceIdRatioBasedSampler) {
      logger.warning(
          "TraceIdRatioBasedSampler is being used as a child sampler ("
              + field
              + "). "
              + "This configuration is discouraged per the OpenTelemetry specification "
              + "and may lead to unexpected sampling behavior.");
    }
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
