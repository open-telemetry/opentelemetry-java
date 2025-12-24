/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A Sampler that uses the sampled flag of the parent Span, if present. If the span has no parent,
 * this Sampler will use the "root" sampler that it is built with. See documentation on the {@link
 * ParentBasedSamplerBuilder} methods for the details on the various configurable options.
 */
@Immutable
final class ParentBasedSampler implements Sampler {

  private final Sampler root;
  private final Sampler remoteParentSampled;
  private final Sampler remoteParentNotSampled;
  private final Sampler localParentSampled;
  private final Sampler localParentNotSampled;

  ParentBasedSampler(
      Sampler root,
      @Nullable Sampler remoteParentSampled,
      @Nullable Sampler remoteParentNotSampled,
      @Nullable Sampler localParentSampled,
      @Nullable Sampler localParentNotSampled) {
    this.root = root;
    this.remoteParentSampled =
        remoteParentSampled == null ? Sampler.alwaysOn() : remoteParentSampled;
    this.remoteParentNotSampled =
        remoteParentNotSampled == null ? Sampler.alwaysOff() : remoteParentNotSampled;
    this.localParentSampled = localParentSampled == null ? Sampler.alwaysOn() : localParentSampled;
    this.localParentNotSampled =
        localParentNotSampled == null ? Sampler.alwaysOff() : localParentNotSampled;
  }

  // If a parent is set, always follows the same sampling decision as the parent.
  // Otherwise, uses the delegateSampler provided at initialization to make a decision.
  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    SpanContext parentSpanContext = Span.fromContext(parentContext).getSpanContext();
    if (!parentSpanContext.isValid()) {
      return this.root.shouldSample(
          parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    if (parentSpanContext.isRemote()) {
      return parentSpanContext.isSampled()
          ? this.remoteParentSampled.shouldSample(
              parentContext, traceId, name, spanKind, attributes, parentLinks)
          : this.remoteParentNotSampled.shouldSample(
              parentContext, traceId, name, spanKind, attributes, parentLinks);
    }
    return parentSpanContext.isSampled()
        ? this.localParentSampled.shouldSample(
            parentContext, traceId, name, spanKind, attributes, parentLinks)
        : this.localParentNotSampled.shouldSample(
            parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  @Override
  public String getDescription() {
    return String.format(
        "ParentBased{root:%s,remoteParentSampled:%s,remoteParentNotSampled:%s,"
            + "localParentSampled:%s,localParentNotSampled:%s}",
        this.root.getDescription(),
        this.remoteParentSampled.getDescription(),
        this.remoteParentNotSampled.getDescription(),
        this.localParentSampled.getDescription(),
        this.localParentNotSampled.getDescription());
  }

  @Override
  public String toString() {
    return getDescription();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ParentBasedSampler)) {
      return false;
    }

    ParentBasedSampler that = (ParentBasedSampler) o;

    return root.equals(that.root)
        && remoteParentSampled.equals(that.remoteParentSampled)
        && remoteParentNotSampled.equals(that.remoteParentNotSampled)
        && localParentSampled.equals(that.localParentSampled)
        && localParentNotSampled.equals(that.localParentNotSampled);
  }

  @Override
  public int hashCode() {
    int result = root.hashCode();
    result = 31 * result + remoteParentSampled.hashCode();
    result = 31 * result + remoteParentNotSampled.hashCode();
    result = 31 * result + localParentSampled.hashCode();
    result = 31 * result + localParentNotSampled.hashCode();
    return result;
  }
}
