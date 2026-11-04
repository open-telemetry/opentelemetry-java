/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.INVALID_THRESHOLD;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.MIN_THRESHOLD;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.NO_TRACE_STATE_UPDATE;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.isValidThreshold;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.util.List;

final class ComposableParentThresholdSampler implements ComposableSampler {

  private final ComposableSampler rootSampler;
  private final String description;

  ComposableParentThresholdSampler(ComposableSampler rootSampler) {
    this.rootSampler = rootSampler;
    this.description = "ComposableParentThresholdSampler{rootSampler=" + rootSampler + "}";
  }

  @Override
  public SamplingIntent getSamplingIntent(
      Context parentContext,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    SpanContext parentSpanContext = Span.fromContext(parentContext).getSpanContext();
    if (!parentSpanContext.isValid()) {
      return rootSampler.getSamplingIntent(parentContext, name, spanKind, attributes, parentLinks);
    }

    boolean parentSampled = parentSpanContext.getTraceFlags().isSampled();
    OtelTraceState otTraceState = OtelTraceState.parse(parentSpanContext.getTraceState());
    long parentThreshold = otTraceState.getThreshold();
    if (isValidThreshold(parentThreshold)
        && (!parentSampled
            || parentThreshold
                <= otTraceState.getRandomValueOrFromTraceId(parentSpanContext.getTraceId()))) {
      return ImmutableSamplingIntent.create(
          parentThreshold,
          /* adjustedCountReliable= */ true,
          Attributes.empty(),
          NO_TRACE_STATE_UPDATE);
    }

    long threshold = parentSampled ? MIN_THRESHOLD : INVALID_THRESHOLD;
    return ImmutableSamplingIntent.create(
        threshold, /* adjustedCountReliable= */ false, Attributes.empty(), NO_TRACE_STATE_UPDATE);
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return this.getDescription();
  }
}
