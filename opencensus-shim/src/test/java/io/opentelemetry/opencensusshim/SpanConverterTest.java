/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.opencensus.trace.SpanContext;
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
                TraceState.builder().set(traceStateKey, traceStateValue).build()));

    io.opencensus.trace.Span ocSPan = SpanConverter.fromOtelSpan(otelSpan);

    SpanContext context = ocSPan.getContext();
    assertThat(context.getTraceId().toLowerBase16()).isEqualTo(traceIdHex);
    assertThat(context.getSpanId().toLowerBase16()).isEqualTo(spanIdHex);
    assertThat(context.getTraceOptions().isSampled()).isTrue();
    assertThat(context.getTracestate().getEntries().size()).isEqualTo(1);
    assertThat(context.getTracestate().get(traceStateKey)).isEqualTo(traceStateValue);
  }
}
