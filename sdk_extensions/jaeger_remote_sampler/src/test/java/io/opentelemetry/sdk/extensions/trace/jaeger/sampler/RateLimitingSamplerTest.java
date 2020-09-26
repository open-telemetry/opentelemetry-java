/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.extensions.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.trace.Sampler.Decision;
import io.opentelemetry.sdk.trace.Sampler.SamplingResult;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class RateLimitingSamplerTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final Span.Kind SPAN_KIND = Span.Kind.INTERNAL;
  private final String traceId = TraceId.fromLongs(150, 150);
  private final String parentSpanId = SpanId.fromLong(250);
  private final TraceState traceState = TraceState.builder().build();
  private final Span sampledSpan =
      Span.getPropagated(traceId, parentSpanId, TraceFlags.getSampled(), traceState);
  private final Span notSampledSpan = Span.getUnsampled(traceId, parentSpanId, traceState);

  @Test
  void alwaysSampleSampledContext() {
    RateLimitingSampler sampler = new RateLimitingSampler(1);
    assertThat(
            sampler
                .shouldSample(
                    sampledSpan,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);
    assertThat(
            sampler
                .shouldSample(
                    sampledSpan,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.RECORD_AND_SAMPLE);
  }

  @Test
  void sampleOneTrace() {
    RateLimitingSampler sampler = new RateLimitingSampler(1);
    SamplingResult samplingResult =
        sampler.shouldSample(
            notSampledSpan,
            traceId,
            SPAN_NAME,
            SPAN_KIND,
            Attributes.empty(),
            Collections.emptyList());
    assertThat(samplingResult.getDecision()).isEqualTo(Decision.RECORD_AND_SAMPLE);
    assertThat(
            sampler
                .shouldSample(
                    notSampledSpan,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(Decision.DROP);
    assertEquals(2, samplingResult.getAttributes().size());
    assertEquals(1d, samplingResult.getAttributes().get(RateLimitingSampler.SAMPLER_PARAM));
    assertEquals(
        RateLimitingSampler.TYPE,
        samplingResult.getAttributes().get(RateLimitingSampler.SAMPLER_TYPE));
  }

  @Test
  void description() {
    RateLimitingSampler sampler = new RateLimitingSampler(15);
    assertEquals("RateLimitingSampler{15.00}", sampler.getDescription());
  }
}
