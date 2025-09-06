/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.INVALID_THRESHOLD;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.MIN_THRESHOLD;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.isValidThreshold;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.util.List;
import java.util.function.Function;

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
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    SpanContext parentSpanContext = Span.fromContext(parentContext).getSpanContext();
    if (!parentSpanContext.isValid()) {
      return rootSampler.getSamplingIntent(
          parentContext, traceId, name, spanKind, attributes, parentLinks);
    }

    OtelTraceState otTraceState = OtelTraceState.parse(parentSpanContext.getTraceState());
    if (isValidThreshold(otTraceState.getThreshold())) {
      return ImmutableSamplingIntent.create(
          otTraceState.getThreshold(),
          /* thresholdReliable= */ true,
          Attributes.empty(),
          Function.identity());
    }

    long threshold =
        parentSpanContext.getTraceFlags().isSampled() ? MIN_THRESHOLD : INVALID_THRESHOLD;
    return ImmutableSamplingIntent.create(
        threshold, /* thresholdReliable= */ false, Attributes.empty(), Function.identity());
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
