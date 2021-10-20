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

class AlwaysOnSamplerTest {

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
  void parentSampled() {
    assertThat(
            Sampler.alwaysOn()
                .shouldSample(
                    sampledParentContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
  }

  @Test
  void parentNotSampled() {
    assertThat(
            Sampler.alwaysOn()
                .shouldSample(
                    notSampledParentContext,
                    traceId,
                    SPAN_NAME,
                    SPAN_KIND,
                    Attributes.empty(),
                    Collections.emptyList())
                .getDecision())
        .isEqualTo(SamplingDecision.RECORD_AND_SAMPLE);
  }

  @Test
  void getDescription() {
    assertThat(Sampler.alwaysOn().getDescription()).isEqualTo("AlwaysOnSampler");
  }

  @Test
  void string() {
    assertThat(Sampler.alwaysOn().toString()).isEqualTo("AlwaysOnSampler");
  }
}
