/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.IdGenerator;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class AlwaysOffSamplerTest {

  private static final String SPAN_NAME = "MySpanName";
  private static final Span.Kind SPAN_KIND = Span.Kind.INTERNAL;
  private final IdGenerator idsGenerator = IdGenerator.random();
  private final String traceId = idsGenerator.generateTraceId();
  private final String parentSpanId = idsGenerator.generateSpanId();
  private final TraceState traceState = TraceState.builder().build();
  private final SpanContext sampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getSampled(), traceState);
  private final Context sampledParentContext = Context.root().with(Span.wrap(sampledSpanContext));
  private final Context notSampledParentContext =
      Context.root()
          .with(
              Span.wrap(
                  SpanContext.create(traceId, parentSpanId, TraceFlags.getDefault(), traceState)));

  @Test
  void parentNotSampled() {
    assertThat(
            Sampler.alwaysOff()
                .shouldSample(
                    sampledParentContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingResult.Decision.DROP);
  }

  @Test
  void parentSampled() {
    assertThat(
            Sampler.alwaysOff()
                .shouldSample(
                    notSampledParentContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingResult.Decision.DROP);
  }

  @Test
  void getDescription() {
    assertThat(Sampler.alwaysOff().getDescription()).isEqualTo("AlwaysOffSampler");
  }
}
