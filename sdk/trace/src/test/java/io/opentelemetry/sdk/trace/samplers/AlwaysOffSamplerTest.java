/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.IdGenerator;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class AlwaysOffSamplerTest {

  private static final String SPAN_NAME = "MySpanName";
  private static final SpanKind SPAN_KIND = SpanKind.INTERNAL;
  private final IdGenerator idsGenerator = IdGenerator.random();
  private final String traceId = idsGenerator.generateTraceId();
  private final String parentSpanId = idsGenerator.generateSpanId();
  private final SpanContext sampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceFlags.getSampled(), TraceState.getDefault());
  private final Context sampledParentContext = Context.groot().with(Span.wrap(sampledSpanContext));
  private final Context notSampledParentContext =
      Context.groot()
          .with(
              Span.wrap(
                  SpanContext.create(
                      traceId, parentSpanId, TraceFlags.getDefault(), TraceState.getDefault())));

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
        .isEqualTo(SamplingDecision.DROP);
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
        .isEqualTo(SamplingDecision.DROP);
  }

  @Test
  void getDescription() {
    assertThat(Sampler.alwaysOff().getDescription()).isEqualTo("AlwaysOffSampler");
  }

  @Test
  void string() {
    assertThat(Sampler.alwaysOff().toString()).isEqualTo("AlwaysOffSampler");
  }
}
