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

package io.opentelemetry.trace.util;

import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.Sampler.Decision;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Static class to access a set of pre-defined {@link Sampler Samplers}.
 *
 * @since 0.1.0
 */
public final class Samplers {
  private static final Sampler ALWAYS_SAMPLE = new AlwaysSampleSampler();
  private static final Sampler NEVER_SAMPLE = new NeverSampleSampler();
  private static final Decision ALWAYS_ON_DECISION = new SimpleDecision(/* decision= */ true);
  private static final Decision ALWAYS_OFF_DECISION = new SimpleDecision(/* decision= */ false);

  // No instance of this class.
  private Samplers() {}

  /**
   * Returns a {@link Sampler} that always makes a "yes" decision on {@link Span} sampling.
   *
   * @return a {@code Sampler} that always makes a "yes" decision on {@code Span} sampling.
   * @since 0.1.0
   */
  public static Sampler alwaysSample() {
    return ALWAYS_SAMPLE;
  }

  /**
   * Returns a {@link Sampler} that always makes a "no" decision on {@link Span} sampling.
   *
   * @return a {@code Sampler} that always makes a "no" decision on {@code Span} sampling.
   * @since 0.1.0
   */
  public static Sampler neverSample() {
    return NEVER_SAMPLE;
  }

  @Immutable
  private static final class AlwaysSampleSampler implements Sampler {
    AlwaysSampleSampler() {}

    // Returns always makes a "yes" decision on {@link Span} sampling.
    @Override
    public Decision shouldSample(
        @Nullable SpanContext parentContext,
        @Nullable Boolean hasRemoteParent,
        TraceId traceId,
        SpanId spanId,
        String name,
        List<SpanContext> parentLinks) {
      return ALWAYS_ON_DECISION;
    }

    @Override
    public String getDescription() {
      return toString();
    }

    @Override
    public String toString() {
      return "AlwaysSampleSampler";
    }
  }

  @Immutable
  private static final class NeverSampleSampler implements Sampler {
    NeverSampleSampler() {}

    // Returns always makes a "no" decision on {@link Span} sampling.
    @Override
    public Decision shouldSample(
        @Nullable SpanContext parentContext,
        @Nullable Boolean hasRemoteParent,
        TraceId traceId,
        SpanId spanId,
        String name,
        List<SpanContext> parentLinks) {
      return ALWAYS_OFF_DECISION;
    }

    @Override
    public String getDescription() {
      return toString();
    }

    @Override
    public String toString() {
      return "NeverSampleSampler";
    }
  }

  /** Sampling decision without attributes. */
  @Immutable
  private static final class SimpleDecision implements Decision {

    private final boolean decision;

    /**
     * Creates sampling decision without attributes.
     *
     * @param decision sampling decision
     */
    SimpleDecision(boolean decision) {
      this.decision = decision;
    }

    @Override
    public boolean isSampled() {
      return decision;
    }

    @Override
    public Map<String, AttributeValue> attributes() {
      return Collections.emptyMap();
    }
  }
}
