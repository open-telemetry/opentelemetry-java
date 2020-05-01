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

package io.opentelemetry.sdk.contrib.trace.jaeger.sampler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.trace.Sampler.Decision;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RateLimitingSamplerTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final Span.Kind SPAN_KIND = Span.Kind.INTERNAL;
  private final TraceId traceId = new TraceId(150, 150);
  private final SpanId spanId = new SpanId(150);
  private final SpanId parentSpanId = new SpanId(250);
  private final TraceState traceState = TraceState.builder().build();
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          traceId, parentSpanId, TraceFlags.builder().setIsSampled(true).build(), traceState);
  private final SpanContext notSampledSpanContext =
      SpanContext.create(
          traceId, parentSpanId, TraceFlags.builder().setIsSampled(false).build(), traceState);

  @Test
  public void alwaysSampleSampledContext() {
    RateLimitingSampler sampler = new RateLimitingSampler(1);
    assertTrue(
        sampler
            .shouldSample(
                sampledSpanContext,
                traceId,
                spanId,
                SPAN_NAME,
                SPAN_KIND,
                Collections.<String, AttributeValue>emptyMap(),
                Collections.<Link>emptyList())
            .isSampled());
    assertTrue(
        sampler
            .shouldSample(
                sampledSpanContext,
                traceId,
                spanId,
                SPAN_NAME,
                SPAN_KIND,
                Collections.<String, AttributeValue>emptyMap(),
                Collections.<Link>emptyList())
            .isSampled());
  }

  @Test
  public void sampleOneTrace() {
    RateLimitingSampler sampler = new RateLimitingSampler(1);
    Decision decision =
        sampler.shouldSample(
            notSampledSpanContext,
            traceId,
            spanId,
            SPAN_NAME,
            SPAN_KIND,
            Collections.<String, AttributeValue>emptyMap(),
            Collections.<Link>emptyList());
    assertTrue(decision.isSampled());
    assertFalse(
        sampler
            .shouldSample(
                notSampledSpanContext,
                traceId,
                spanId,
                SPAN_NAME,
                SPAN_KIND,
                Collections.<String, AttributeValue>emptyMap(),
                Collections.<Link>emptyList())
            .isSampled());
    assertEquals(2, decision.getAttributes().size());
    assertEquals(
        AttributeValue.doubleAttributeValue(1),
        decision.getAttributes().get(RateLimitingSampler.SAMPLER_PARAM));
    assertEquals(
        AttributeValue.stringAttributeValue(RateLimitingSampler.TYPE),
        decision.getAttributes().get(RateLimitingSampler.SAMPLER_TYPE));
  }

  @Test
  public void description() {
    RateLimitingSampler sampler = new RateLimitingSampler(15);
    assertEquals("RateLimitingSampler{15.00}", sampler.getDescription());
  }
}
