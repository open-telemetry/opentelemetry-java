/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static io.opentelemetry.extension.trace.propagation.SpanLinkingMultiTextMapPropagator.SPAN_LINKS;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.sdk.trace.IdGenerator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

class SpanLinkingMultiTextMapPropagatorTest {

  private static final TextMapGetter<Map<String, String>> MAP_GETTER =
      new TextMapGetter<Map<String, String>>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
          return carrier.keySet();
        }

        @Nullable
        @Override
        public String get(@Nullable Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  @Test
  void test() {
    String field1 = "field1";
    String field2 = "field2";
    String field3 = "field3";
    TestPropagator propagator1 = new TestPropagator(field1);
    TestPropagator propagator2 = new TestPropagator(field2);
    TestPropagator propagator3 = new TestPropagator(field3);
    SpanContext spanContext1 =
        SpanContext.create(
            IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(),
            TraceFlags.getDefault(),
            TraceState.getDefault());
    SpanContext spanContext2 =
        SpanContext.create(
            IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(),
            TraceFlags.getDefault(),
            TraceState.getDefault());
    SpanContext spanContext3 =
        SpanContext.create(
            IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(),
            TraceFlags.getDefault(),
            TraceState.getDefault());
    Map<String, String> carrier =
        ImmutableMap.of(
            field1,
            encodeTraceParent(spanContext1),
            field2,
            encodeTraceParent(spanContext2),
            field3,
            encodeTraceParent(spanContext3));

    TextMapPropagator propagator1Priority =
        new SpanLinkingMultiTextMapPropagator(Arrays.asList(propagator1, propagator2, propagator3));
    TextMapPropagator propagator2Priority =
        new SpanLinkingMultiTextMapPropagator(Arrays.asList(propagator2, propagator1, propagator3));

    // Extract context with propagator1Priority, which prioritizes span context from field1. The
    // contexts in field2 and field3 should end up as links.
    Context result = propagator1Priority.extract(Context.root(), carrier, MAP_GETTER);
    assertThat(Span.fromContext(result).getSpanContext()).isEqualTo(spanContext1);
    assertThat(result.get(SPAN_LINKS)).isEqualTo(Arrays.asList(spanContext2, spanContext3));

    // Extract context with propagator2Priority, which prioritizes span context from field2. The
    // contexts in field1 and field3 should end up as links.
    result = propagator2Priority.extract(Context.root(), carrier, MAP_GETTER);
    assertThat(Span.fromContext(result).getSpanContext()).isEqualTo(spanContext2);
    assertThat(result.get(SPAN_LINKS)).isEqualTo(Arrays.asList(spanContext1, spanContext3));
  }

  private static class TestPropagator implements TextMapPropagator {

    private final String traceParentField;

    private TestPropagator(String traceParentField) {
      this.traceParentField = traceParentField;
    }

    @Override
    public Collection<String> fields() {
      return Collections.singletonList(traceParentField);
    }

    @Override
    public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {}

    @Override
    public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
      String traceParent = getter.get(carrier, traceParentField);
      if (traceParent == null) {
        return context;
      }
      return context.with(Span.wrap(decodeTraceParent(traceParent)));
    }
  }

  private static String encodeTraceParent(SpanContext spanContext) {
    return spanContext.getTraceId() + "|" + spanContext.getSpanId();
  }

  private static SpanContext decodeTraceParent(String traceParent) {
    String[] parts = traceParent.split("\\|");
    if (parts.length != 2) {
      return SpanContext.getInvalid();
    }
    return SpanContext.create(parts[0], parts[1], TraceFlags.getDefault(), TraceState.getDefault());
  }
}
