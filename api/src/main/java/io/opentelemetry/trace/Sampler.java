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

package io.opentelemetry.trace;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Sampler is used to make decisions on {@link Span} sampling.
 *
 * @since 0.1.0
 */
public interface Sampler {
  /**
   * Called during {@link Span} creation to make a sampling decision.
   *
   * @param parentContext the parent span's {@link SpanContext}. {@code null} if this is a root
   *     span.
   * @param hasRemoteParent {@code true} if the parent {@code Span} is remote. {@code null} if this
   *     is a root span.
   * @param traceId the {@link TraceId} for the new {@code Span}. This will be identical to that in
   *     the parentContext, unless this is a root span.
   * @param spanId the {@link SpanId} for the new {@code Span}.
   * @param name the name of the new {@code Span}.
   * @param parentLinks the parentLinks associated with the new {@code Span}.
   * @return sampling decision whether span should be sampled or not.
   * @since 0.1.0
   */
  SamplingDecision shouldSample(
      @Nullable SpanContext parentContext,
      @Nullable Boolean hasRemoteParent,
      TraceId traceId,
      SpanId spanId,
      String name,
      List<Span> parentLinks);

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
   * Sampling decision returned by {@link Sampler#shouldSample(SpanContext, Boolean, TraceId,
   * SpanId, String, List)}.
   *
   * @since 0.1.0
   */
  interface SamplingDecision {

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
     * @return attributes which will be added to the span.
     * @since 0.1.0
     */
    Map<String, AttributeValue> attributes();
  }
}
