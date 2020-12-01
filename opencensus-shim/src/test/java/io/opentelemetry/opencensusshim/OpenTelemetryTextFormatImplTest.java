/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.propagation.TextFormat.Getter;
import io.opencensus.trace.propagation.TextFormat.Setter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OpenTelemetryTextFormatImplTest {
  private static final Setter<Map<String, String>> SETTER =
      new Setter<Map<String, String>>() {
        @Override
        public void put(Map<String, String> carrier, String key, String value) {
          carrier.put(key, value);
        }
      };
  private static final Getter<Map<String, String>> GETTER =
      new Getter<Map<String, String>>() {
        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  private static final Random RANDOM = new Random();
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(
          TraceId.generateRandomId(RANDOM),
          SpanId.generateRandomId(RANDOM),
          TraceOptions.builder().setIsSampled(true).build(),
          Tracestate.builder().set("key", "value").build());

  @Test
  public void testInject() {
    TextMapPropagator propagator = spy(TextMapPropagator.class);
    OpenTelemetryTextFormatImpl textFormatImpl = new OpenTelemetryTextFormatImpl(propagator);
    Map<String, String> carrier = new LinkedHashMap<>();

    textFormatImpl.inject(SPAN_CONTEXT, carrier, SETTER);

    ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
    verify(propagator, times(1)).inject(contextCaptor.capture(), any(), any());
    assertThat(Span.fromContext(contextCaptor.getValue()).getSpanContext())
        .isEqualTo(SpanConverter.mapSpanContext(SPAN_CONTEXT));
  }

  @Test
  public void testInjectWithNotSampledContext() {
    TextMapPropagator propagator = spy(TextMapPropagator.class);
    OpenTelemetryTextFormatImpl textFormatImpl = new OpenTelemetryTextFormatImpl(propagator);
    SpanContext spanContext =
        SpanContext.create(
            TraceId.generateRandomId(RANDOM),
            SpanId.generateRandomId(RANDOM),
            TraceOptions.builder().setIsSampled(false).build(),
            Tracestate.builder().build());
    Map<String, String> carrier = new LinkedHashMap<>();

    textFormatImpl.inject(spanContext, carrier, SETTER);

    ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
    verify(propagator, times(1)).inject(contextCaptor.capture(), any(), any());
    assertThat(Span.fromContext(contextCaptor.getValue()).getSpanContext())
        .isEqualTo(SpanConverter.mapSpanContext(spanContext));
  }

  @Test
  public void testInjectWithDefaultOptions() {
    TextMapPropagator propagator = spy(TextMapPropagator.class);
    OpenTelemetryTextFormatImpl textFormatImpl = new OpenTelemetryTextFormatImpl(propagator);
    SpanContext spanContext =
        SpanContext.create(
            TraceId.generateRandomId(RANDOM),
            SpanId.generateRandomId(RANDOM),
            TraceOptions.DEFAULT,
            Tracestate.builder().build());
    Map<String, String> carrier = new LinkedHashMap<>();

    textFormatImpl.inject(spanContext, carrier, SETTER);

    ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
    verify(propagator, times(1)).inject(contextCaptor.capture(), any(), any());
    assertThat(Span.fromContext(contextCaptor.getValue()).getSpanContext())
        .isEqualTo(SpanConverter.mapSpanContext(spanContext));
  }

  @Test
  public void testInjectAndExtractWithB3() {
    OpenTelemetryTextFormatImpl textFormatImpl =
        new OpenTelemetryTextFormatImpl(B3Propagator.builder().injectMultipleHeaders().build());
    Map<String, String> carrier = new LinkedHashMap<>();

    textFormatImpl.inject(SPAN_CONTEXT, carrier, SETTER);

    SpanContext extractedSpanContext = textFormatImpl.extract(carrier, GETTER);
    assertThat(extractedSpanContext).isEqualTo(SPAN_CONTEXT);
  }

  @Test
  public void testInjectAndExtractWithW3c() {
    OpenTelemetryTextFormatImpl textFormatImpl =
        new OpenTelemetryTextFormatImpl(W3CTraceContextPropagator.getInstance());
    Map<String, String> carrier = new LinkedHashMap<>();

    textFormatImpl.inject(SPAN_CONTEXT, carrier, SETTER);

    SpanContext extractedSpanContext = textFormatImpl.extract(carrier, GETTER);
    assertThat(extractedSpanContext).isEqualTo(SPAN_CONTEXT);
  }
}
