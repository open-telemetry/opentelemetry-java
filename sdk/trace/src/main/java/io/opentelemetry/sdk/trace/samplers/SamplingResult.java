/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import java.util.List;

/**
 * Sampling result returned by {@link Sampler#shouldSample(Context, String, String, Span.Kind,
 * Attributes, List)}.
 */
public interface SamplingResult {

  /**
   * Returns a {@link SamplingResult} with no attributes and {@link SamplingResult#getDecision()}
   * returning {@code decision}.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * <p>Use {@link #create(Decision, Attributes)} if you need attributes.
   *
   * @param decision The decision made on the span.
   * @return A {@link SamplingResult} with empty attributes and the provided {@code decision}.
   */
  static SamplingResult create(Decision decision) {
    switch (decision) {
      case RECORD_AND_SAMPLE:
        return ImmutableSamplingResult.EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT;
      case RECORD_ONLY:
        return ImmutableSamplingResult.EMPTY_RECORDED_SAMPLING_RESULT;
      case DROP:
        return ImmutableSamplingResult.EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT;
    }
    throw new AssertionError("unrecognised samplingResult");
  }

  /**
   * Returns a {@link SamplingResult} with the given {@code attributes} and {@link
   * SamplingResult#getDecision()} returning {@code decision}.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * <p>Using {@link #create(Decision)} instead of this method is slightly faster and shorter if you
   * don't need attributes.
   *
   * @param decision The decision made on the span.
   * @param attributes The attributes to return from {@link SamplingResult#getAttributes()}. A
   *     different object instance with the same elements may be returned.
   * @return A {@link SamplingResult} with the attributes equivalent to {@code attributes} and the
   *     provided {@code decision}.
   */
  static SamplingResult create(Decision decision, Attributes attributes) {
    requireNonNull(attributes, "attributes");
    return attributes.isEmpty()
        ? create(decision)
        : ImmutableSamplingResult.createSamplingResult(decision, attributes);
  }

  /** A decision on whether a span should be recorded, recorded and sampled or dropped. */
  enum Decision {
    DROP,
    RECORD_ONLY,
    RECORD_AND_SAMPLE,
  }

  /**
   * Return decision on whether a span should be recorded, recorded and sampled or not recorded.
   *
   * @return sampling result.
   */
  Decision getDecision();

  /**
   * Return tags which will be attached to the span.
   *
   * @return attributes added to span. These attributes should be added to the span only when
   *     {@linkplain #getDecision() the sampling decision} is {@link Decision#RECORD_ONLY} or {@link
   *     Decision#RECORD_AND_SAMPLE}.
   */
  Attributes getAttributes();

  /**
   * Return an optionally-updated {@link TraceState}, based on the parent TraceState. This may
   * return the same {@link TraceState} that was provided originally, or an updated one.
   *
   * @param parentTraceState The TraceState from the parent span. Might be an empty TraceState, if
   *     there is no parent. This will be the same TraceState that was passed in via the {@link
   *     SpanContext} parameter on the {@link Sampler#shouldSample(Context, String, String,
   *     Span.Kind, Attributes, List)} call.
   */
  default TraceState getUpdatedTraceState(TraceState parentTraceState) {
    return parentTraceState;
  }
}
