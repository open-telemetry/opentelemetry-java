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
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Sampler is used to make decisions on {@link Span} sampling.
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface Sampler {
  /**
   * Called during {@link Span} creation to make a sampling decision.
   *
   * @param parentContext the parent span's {@link SpanContext}. {@code null} if this is a root
   *     span.
   * @param traceId the {@link TraceId} for the new {@code Span}. This will be identical to that in
   *     the parentContext, unless this is a root span.
   * @param spanId the {@link SpanId} for the new {@code Span}.
   * @param name the name of the new {@code Span}.
   * @param parentLinks the parentLinks associated with the new {@code Span}.
   * @param spanKind the {@link Span.Kind} of the {@code Span}.
   * @param attributes list of {@link AttributeValue} with their keys.
   * @return sampling decision whether span should be sampled or not.
   * @since 0.1.0
   */
  Decision shouldSample(
      @Nullable SpanContext parentContext,
      TraceId traceId,
      SpanId spanId,
      String name,
      Span.Kind spanKind,
      Map<String, AttributeValue> attributes,
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

  /**
   * Sampling decision returned by {@link Sampler#shouldSample(SpanContext, TraceId, SpanId, String,
   * Span.Kind, Map, List)}.
   *
   * @since 0.1.0
   */
  interface Decision {

    /**
     * Return sampling decision whether span should be sampled or not.
     *
     * @return sampling decision.
     * @since 0.1.0
     */
    boolean isSampled();

    /**
     * Return tags which will be attached to the span.
     *
     * @return attributes added to span. These attributes should be added to the span only for root
     *     span or when sampling decision {@link #isSampled()} changes from false to true.
     * @since 0.1.0
     */
    Map<String, AttributeValue> getAttributes();
  }
}
