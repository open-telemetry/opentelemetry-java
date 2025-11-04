/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.INVALID_THRESHOLD;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.isValidRandomValue;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.ImmutableSamplingIntent.isValidThreshold;
import static io.opentelemetry.sdk.extension.incubator.trace.samplers.OtelTraceState.OTEL_TRACE_STATE_KEY;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.RandomSupplier;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.List;

/**
 * A sampler that uses a {@link ComposableSampler} to make its sampling decisions while handlign
 * tracestate.
 */
public final class CompositeSampler implements Sampler {
  /**
   * Returns a new composite {@link Sampler} that delegates to the given {@link ComposableSampler}.
   */
  public static Sampler wrap(ComposableSampler delegate) {
    return new CompositeSampler(delegate);
  }

  private final ComposableSampler delegate;

  private CompositeSampler(ComposableSampler delegate) {
    this.delegate = delegate;
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    TraceState traceState = Span.fromContext(parentContext).getSpanContext().getTraceState();
    OtelTraceState otelTraceState = OtelTraceState.parse(traceState);

    SamplingIntent intent =
        delegate.getSamplingIntent(parentContext, traceId, name, spanKind, attributes, parentLinks);

    boolean thresholdReliable = false;
    boolean sampled = false;
    if (isValidThreshold(intent.getThreshold())) {
      thresholdReliable = intent.isThresholdReliable();
      long randomValue;
      if (thresholdReliable) {
        if (isValidRandomValue(otelTraceState.getRandomValue())) {
          randomValue = otelTraceState.getRandomValue();
        } else {
          // Use last 56 bits of trace ID as random value.
          randomValue = OtelEncodingUtils.longFromBase16String(traceId, 16) & 0x00FFFFFFFFFFFFFFL;
        }
      } else {
        randomValue = RandomSupplier.platformDefault().get().nextLong() & 0x00FFFFFFFFFFFFFFL;
      }
      sampled = intent.getThreshold() <= randomValue;
    }

    SamplingDecision decision =
        sampled ? SamplingDecision.RECORD_AND_SAMPLE : SamplingDecision.DROP;
    if (sampled && thresholdReliable) {
      otelTraceState =
          new OtelTraceState(
              otelTraceState.getRandomValue(), intent.getThreshold(), otelTraceState.getRest());
    } else {
      otelTraceState =
          new OtelTraceState(
              otelTraceState.getRandomValue(), INVALID_THRESHOLD, otelTraceState.getRest());
    }

    String serializedState = otelTraceState.serialize();
    return new SamplingResult() {
      @Override
      public SamplingDecision getDecision() {
        return decision;
      }

      @Override
      public Attributes getAttributes() {
        return intent.getAttributes();
      }

      @Override
      public TraceState getUpdatedTraceState(TraceState parentTraceState) {
        TraceState newTraceState = intent.getTraceStateUpdater().apply(traceState);
        if (!serializedState.isEmpty()) {
          newTraceState =
              newTraceState.toBuilder().put(OTEL_TRACE_STATE_KEY, serializedState).build();
        }
        return newTraceState;
      }
    };
  }

  @Override
  public String getDescription() {
    return delegate.getDescription();
  }

  @Override
  public String toString() {
    return this.getDescription();
  }
}
