/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.TraceId;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Sampler is used to make decisions on {@link Span} sampling.
 *
 * @since 0.1.0
 */
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
   * @param attributes list of {@link AttributeValue} with their keys.
   * @param parentLinks the parentLinks associated with the new {@code Span}.
   * @return sampling samplingResult whether span should be sampled or not.
   * @since 0.1.0
   */
  SamplingResult shouldSample(
      SpanContext parentContext,
      CharSequence traceId,
      String name,
      Kind spanKind,
      ReadableAttributes attributes,
      List<Link> parentLinks);

  /**
   * Returns the description of this {@code Sampler}. This may be displayed on debug pages or in the
   * logs.
   *
   * <p>Example: "ProbabilitySampler{0.000100}"
   *
   * @return the description of this {@code Sampler}.
   * @since 0.1.0
   */
  String getDescription();

  /** A decision on whether a span should be recorded, recorded and sampled or not recorded. */
  enum Decision {
    NOT_RECORD,
    RECORD,
    RECORD_AND_SAMPLED,
  }

  /**
   * Sampling result returned by {@link Sampler#shouldSample(SpanContext, CharSequence, String,
   * Kind, ReadableAttributes, List)}.
   *
   * @since 0.1.0
   */
  interface SamplingResult {

    /**
     * Return decision on whether a span should be recorded, recorded and sampled or not recorded.
     *
     * @return sampling result.
     * @since 0.7.0
     */
    Decision getDecision();

    /**
     * Return tags which will be attached to the span.
     *
     * @return attributes added to span. These attributes should be added to the span only when
     *     {@linkplain #getDecision() the sampling decision} is {@link Decision#RECORD} or {@link
     *     Decision#RECORD_AND_SAMPLED}.
     * @since 0.1.0
     */
    Attributes getAttributes();
  }
}
