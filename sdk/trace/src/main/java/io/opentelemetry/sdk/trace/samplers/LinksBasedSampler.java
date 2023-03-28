/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import io.opentelemetry.api.common.Attributes;
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
final class LinksBasedSampler implements Sampler {

  private final Sampler root;

  private static final SamplingResult POSITIVE_SAMPLING_RESULT = SamplingResult.recordAndSample();
  private static final SamplingResult NEGATIVE_SAMPLING_RESULT = SamplingResult.drop();

  LinksBasedSampler(Sampler root) {
    this.root = root;
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
    if (parentLinks.size() > 0) {
      for (LinkData linkData : parentLinks) {
        if (linkData.getSpanContext().isSampled()) {
          return POSITIVE_SAMPLING_RESULT;
        }
      }
      return NEGATIVE_SAMPLING_RESULT;
    }

    return this.root.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  @Override
  public String getDescription() {
    return String.format("LinksBased{root:%s}", this.root.getDescription());
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
    if (!(o instanceof LinksBasedSampler)) {
      return false;
    }

    LinksBasedSampler that = (LinksBasedSampler) o;

    return root.equals(that.root);
  }

  @Override
  public int hashCode() {
    return root.hashCode();
  }
}
