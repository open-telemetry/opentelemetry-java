/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.TraceId;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;

/** Sampler is used to make decisions on {@link Span} sampling. */
@ThreadSafe
public interface Sampler {
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
      SpanContext parentContext,
      String traceId,
      String name,
      Kind spanKind,
      ReadableAttributes attributes,
      List<SpanData.Link> parentLinks);

  /**
   * Returns the description of this {@code Sampler}. This may be displayed on debug pages or in the
   * logs.
   *
   * <p>Example: "TraceIdRatioBased{0.000100}"
   *
   * @return the description of this {@code Sampler}.
   */
  String getDescription();

  /** A decision on whether a span should be recorded, recorded and sampled or dropped. */
  enum Decision {
    DROP,
    RECORD_ONLY,
    RECORD_AND_SAMPLE,
  }

  /**
   * Sampling result returned by {@link Sampler#shouldSample(SpanContext, String, String, Kind,
   * ReadableAttributes, List)}.
   */
  interface SamplingResult {

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
     *     {@linkplain #getDecision() the sampling decision} is {@link Decision#RECORD_ONLY} or
     *     {@link Decision#RECORD_AND_SAMPLE}.
     */
    Attributes getAttributes();
  }
}
