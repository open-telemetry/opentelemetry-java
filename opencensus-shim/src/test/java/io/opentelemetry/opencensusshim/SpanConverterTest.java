/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracestate;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.trace.IdGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SpanConverterTest {
  private static final IdGenerator RANDOM_IDS_GENERATOR = IdGenerator.random();

  @Test
  void testFromOtelSpan() {
    String traceIdHex = RANDOM_IDS_GENERATOR.generateTraceId();
    String spanIdHex = RANDOM_IDS_GENERATOR.generateSpanId();
    String traceStateKey = "key123";
    String traceStateValue = "value123";

    Span otelSpan = Mockito.mock(Span.class);
    when(otelSpan.getSpanContext())
        .thenReturn(
            io.opentelemetry.api.trace.SpanContext.create(
                traceIdHex,
                spanIdHex,
                TraceFlags.getSampled(),
                TraceState.builder().put(traceStateKey, traceStateValue).build()));

    io.opencensus.trace.Span ocSPan = SpanConverter.fromOtelSpan(otelSpan);

    SpanContext context = ocSPan.getContext();
    assertThat(context.getTraceId().toLowerBase16()).isEqualTo(traceIdHex);
    assertThat(context.getSpanId().toLowerBase16()).isEqualTo(spanIdHex);
    assertThat(context.getTraceOptions().isSampled()).isTrue();
    assertThat(context.getTracestate().getEntries().size()).isEqualTo(1);
    assertThat(context.getTracestate().get(traceStateKey)).isEqualTo(traceStateValue);
  }

  @Test
  void testFromOtelSpanDropsMultiTenantTracestateKeys() {
    String traceIdHex = RANDOM_IDS_GENERATOR.generateTraceId();
    String spanIdHex = RANDOM_IDS_GENERATOR.generateSpanId();

    Span otelSpan = Mockito.mock(Span.class);
    when(otelSpan.getSpanContext())
        .thenReturn(
            io.opentelemetry.api.trace.SpanContext.create(
                traceIdHex,
                spanIdHex,
                TraceFlags.getSampled(),
                TraceState.builder()
                    .put("congo", "t61rcWkgMzE")
                    // W3C multi-tenant key, valid in OpenTelemetry but rejected by OpenCensus
                    .put("fw529a3039@dt", "fw4")
                    .build()));

    io.opencensus.trace.Span ocSpan = SpanConverter.fromOtelSpan(otelSpan);

    Tracestate tracestate = ocSpan.getContext().getTracestate();
    assertThat(tracestate.getEntries()).hasSize(1);
    assertThat(tracestate.get("congo")).isEqualTo("t61rcWkgMzE");
    assertThat(tracestate.get("fw529a3039@dt")).isNull();
  }
}
