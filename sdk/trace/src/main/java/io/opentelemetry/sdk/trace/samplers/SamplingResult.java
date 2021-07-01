/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Sampling result returned by {@link Sampler#shouldSample(Context, String, String, SpanKind,
 * Attributes, List)}.
 */
@Immutable
public interface SamplingResult {

  /**
   * Returns a {@link SamplingResult} corresponding to {@link SamplingDecision#RECORD_AND_SAMPLE}
   * with no attributes and {@link SamplingResult#getDecision()} returning {@code decision}.
   *
   * <p>Use {@link #recordAndSample(Attributes)} if you need attributes.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * @return A {@link SamplingResult} with empty attributes and the provided {@code decision}.
   */
  static SamplingResult recordAndSample() {
    return ImmutableSamplingResult.getEmptyRecordedAndSampledSamplingResult();
  }

  /**
   * Returns a {@link SamplingResult} corresponding to {@link SamplingDecision#RECORD_AND_SAMPLE}
   * with the given {@code attributes} and {@link SamplingResult#getDecision()} returning
   * {@code decision}.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * <p>Using {@link #recordAndSample()} instead of this method is slightly faster and
   * shorter if you don't need attributes.
   *
   * @param attributes The attributes to return from {@link SamplingResult#getAttributes()}. A
   *     different object instance with the same elements may be returned.
   * @return A {@link SamplingResult} with the attributes equivalent to {@code attributes} and the
   * {@link SamplingDecision#RECORD_AND_SAMPLE}
   */
  static SamplingResult recordAndSample(Attributes attributes) {
    requireNonNull(attributes, "attributes");
    return attributes.isEmpty()
        ? ImmutableSamplingResult.getEmptyRecordedAndSampledSamplingResult()
        : ImmutableSamplingResult.createSamplingResult(
            SamplingDecision.RECORD_AND_SAMPLE, attributes);
  }

  /**
   * Returns a {@link SamplingResult} corresponding to {@link SamplingDecision#RECORD_ONLY} with
   * no attributes and {@link SamplingResult#getDecision()} returning {@code decision}.
   *
   * <p>Use {@link #recordOnly(Attributes)} if you need attributes.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * @return A {@link SamplingResult} with empty attributes and the provided {@code decision}.
   */
  static SamplingResult recordOnly() {
    return ImmutableSamplingResult.getEmptyRecordedSamplingResult();
  }

  /**
   * Returns a {@link SamplingResult} corresponding to {@link SamplingDecision#RECORD_ONLY} with
   * the given {@code attributes} and {@link SamplingResult#getDecision()} returning
   * {@code decision}.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * <p>Using {@link #recordOnly()}  instead of this method is slightly faster and
   * shorter if you don't need attributes.
   *
   * @param attributes The attributes to return from {@link SamplingResult#getAttributes()}. A
   *     different object instance with the same elements may be returned.
   * @return A {@link SamplingResult} with the attributes equivalent to {@code attributes} and the
   * {@link SamplingDecision#RECORD_ONLY}
   */
  static SamplingResult recordOnly(Attributes attributes) {
    requireNonNull(attributes, "attributes");
    return attributes.isEmpty()
        ? ImmutableSamplingResult.getEmptyRecordedSamplingResult()
        : ImmutableSamplingResult.createSamplingResult(SamplingDecision.RECORD_ONLY, attributes);
  }

  /**
   * Returns a {@link SamplingResult} corresponding to {@link SamplingDecision#DROP} with
   * no attributes and {@link SamplingResult#getDecision()} returning {@code decision}.
   *
   * <p>Use {@link #drop(Attributes)} if you need attributes.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * @return A {@link SamplingResult} with empty attributes and the provided {@code decision}.
   */
  static SamplingResult drop() {
    return ImmutableSamplingResult.getEmptyNotSampledOrRecordedSamplingResult();
  }

  /**
   * Returns a {@link SamplingResult} corresponding to {@link SamplingDecision#DROP} with
   * the given {@code attributes} and {@link SamplingResult#getDecision()} returning
   * {@code decision}.
   *
   * <p>This is meant for use by custom {@link Sampler} implementations.
   *
   * <p>Using {@link #drop()} instead of this method is slightly faster and
   * shorter if you don't need attributes.
   *
   * @param attributes The attributes to return from {@link SamplingResult#getAttributes()}. A
   *     different object instance with the same elements may be returned.
   * @return A {@link SamplingResult} with the attributes equivalent to {@code attributes} and the
   * {@link SamplingDecision#DROP}
   */
  static SamplingResult drop(Attributes attributes) {
    requireNonNull(attributes, "attributes");
    return attributes.isEmpty()
        ? ImmutableSamplingResult.getEmptyNotSampledOrRecordedSamplingResult()
        : ImmutableSamplingResult.createSamplingResult(SamplingDecision.DROP, attributes);
  }

  /**
   * Return decision on whether a span should be recorded, recorded and sampled or not recorded.
   *
   * @return sampling result.
   */
  SamplingDecision getDecision();

  /**
   * Return tags which will be attached to the span.
   *
   * @return attributes added to span. These attributes should be added to the span only when
   *     {@linkplain #getDecision() the sampling decision} is {@link SamplingDecision#RECORD_ONLY}
   *     or {@link SamplingDecision#RECORD_AND_SAMPLE}.
   */
  Attributes getAttributes();

  /**
   * Return an optionally-updated {@link TraceState}, based on the parent TraceState. This may
   * return the same {@link TraceState} that was provided originally, or an updated one.
   *
   * @param parentTraceState The TraceState from the parent span. Might be an empty TraceState, if
   *     there is no parent. This will be the same TraceState that was passed in via the {@link
   *     SpanContext} parameter on the {@link Sampler#shouldSample(Context, String, String,
   *     SpanKind, Attributes, List)} call.
   */
  default TraceState getUpdatedTraceState(TraceState parentTraceState) {
    return parentTraceState;
  }
}
