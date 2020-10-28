/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;

/** Sampler is used to make decisions on {@link Span} sampling. */
@ThreadSafe
public interface Sampler {

  /**
   * Returns a {@link Sampler} that always makes a "yes" {@link SamplingResult} for {@link Span}
   * sampling.
   *
   * @return a {@code Sampler} that always makes a "yes" {@link SamplingResult} for {@code Span}
   *     sampling.
   */
  static Sampler alwaysOn() {
    return AlwaysOnSampler.INSTANCE;
  }

  /**
   * Returns a {@link Sampler} that always makes a "no" {@link SamplingResult} for {@link Span}
   * sampling.
   *
   * @return a {@code Sampler} that always makes a "no" {@link SamplingResult} for {@code Span}
   *     sampling.
   */
  static Sampler alwaysOff() {
    return AlwaysOffSampler.INSTANCE;
  }

  /**
   * Returns a {@link Sampler} that always makes the same decision as the parent {@link Span} to
   * whether or not to sample. If there is no parent, the Sampler uses the provided Sampler delegate
   * to determine the sampling decision.
   *
   * @param root the {@code Sampler} which is used to make the sampling decisions if the parent does
   *     not exist.
   * @return a {@code Sampler} that follows the parent's sampling decision if one exists, otherwise
   *     following the root sampler's decision.
   */
  static Sampler parentBased(Sampler root) {
    return parentBasedBuilder(root).build();
  }

  /**
   * Returns a {@link ParentBasedSampler.Builder} that follows the parent's sampling decision if one
   * exists, otherwise following the root sampler and other optional sampler's decision.
   *
   * @param root the required {@code Sampler} which is used to make the sampling decisions if the
   *     parent does not exist.
   * @return a {@code ParentBased.Builder}
   */
  static ParentBasedSampler.Builder parentBasedBuilder(Sampler root) {
    return new ParentBasedSampler.Builder(root);
  }

  /**
   * Returns a new TraceIdRatioBased {@link Sampler}. The ratio of sampling a trace is equal to that
   * of the specified ratio.
   *
   * @param ratio The desired ratio of sampling. Must be within [0.0, 1.0].
   * @return a new TraceIdRatioBased {@link Sampler}.
   * @throws IllegalArgumentException if {@code ratio} is out of range
   */
  static Sampler traceIdRatioBased(double ratio) {
    return TraceIdRatioBasedSampler.create(ratio);
  }

  /**
   * Called during {@link Span} creation to make a sampling samplingResult.
   *
   * @param parentContext the parent span's {@link SpanContext}. This can be {@code
   *     SpanContext.INVALID} if this is a root span.
   * @param traceId the {@link TraceId} for the new {@code Span}. This will be identical to that in
   *     the parentContext, unless this is a root span.
   * @param name the name of the new {@code Span}.
   * @param spanKind the {@link Kind} of the {@code Span}.
   * @param attributes {@link ReadableAttributes} associated with the span.
   * @param parentLinks the parentLinks associated with the new {@code Span}.
   * @return sampling samplingResult whether span should be sampled or not.
   */
  SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      Kind spanKind,
      ReadableAttributes attributes,
      List<Link> parentLinks);

  /**
   * Returns the description of this {@code Sampler}. This may be displayed on debug pages or in the
   * logs.
   *
   * <p>Example: "TraceIdRatioBased{0.000100}"
   *
   * @return the description of this {@code Sampler}.
   */
  String getDescription();
}
