/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class RateLimitingSamplerTest {

  private static final String SPAN_NAME = "MySpanName";
  private static final SpanKind SPAN_KIND = SpanKind.INTERNAL;
  private final String traceId = TraceId.fromLongs(150, 150);
  private final String parentSpanId = SpanId.fromLong(250);
  private final TraceState traceState = TraceState.builder().build();
  private final Context sampledSpanContext =
      Context.root()
          .with(
              Span.wrap(
                  SpanContext.create(traceId, parentSpanId, TraceFlags.getSampled(), traceState)));
  private final Context notSampledSpanContext =
      Context.root()
          .with(
              Span.wrap(
                  SpanContext.create(traceId, parentSpanId, TraceFlags.getDefault(), traceState)));

  @Test
  void alwaysSampleSampledContext() {
    RateLimitingSampler sampler = new RateLimitingSampler(1);
    assertThat(
            sampler
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(
            sampler
                .shouldSample(
                    sampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
  }

  @Test
  void sampleOneTrace() {
    RateLimitingSampler sampler = new RateLimitingSampler(1);
    SamplingResult samplingResult =
        sampler.shouldSample(
            notSampledSpanContext,
            traceId,
            SPAN_NAME,
            SPAN_KIND,
            Attributes.empty(),
            Collections.emptyList());
    assertThat(samplingResult.getDecision()).isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
    assertThat(
            sampler
                .shouldSample(
                    notSampledSpanContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.DROP);
    assertThat(samplingResult.getAttributes().size()).isEqualTo(2);
    assertThat(samplingResult.getAttributes().get(RateLimitingSampler.SAMPLER_PARAM)).isEqualTo(1d);
    assertThat(samplingResult.getAttributes().get(RateLimitingSampler.SAMPLER_TYPE))
        .isEqualTo(RateLimitingSampler.TYPE);
  }

  @Test
  void description() {
    RateLimitingSampler sampler = new RateLimitingSampler(15);
    assertThat(sampler.getDescription()).isEqualTo("RateLimitingSampler{15.00}");
  }
}
