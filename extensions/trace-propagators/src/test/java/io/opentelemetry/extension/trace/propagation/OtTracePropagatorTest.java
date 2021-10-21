/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class OtTracePropagatorTest {

  private static final String TRACE_ID = "ff000000000000000000000000000041";
  private static final String TRACE_ID_RIGHT_PART = "0000000000000041";
  private static final String SHORT_TRACE_ID = "ff00000000000000";
  private static final String SHORT_TRACE_ID_FULL = "0000000000000000ff00000000000000";
  private static final String SPAN_ID = "ff00000000000041";
  private static final TextMapSetter<Map<String, String>> setter = Map::put;
  private static final TextMapGetter<Map<String, String>> getter =
      new TextMapGetter<Map<String, String>>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
          return carrier.keySet();
        }

        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };
  private final OtTracePropagator propagator = OtTracePropagator.getInstance();

  private static SpanContext getSpanContext(Context context) {
    return Span.fromContext(context).getSpanContext();
  }

  private static Context withSpanContext(SpanContext spanContext, Context context) {
    return context.with(Span.wrap(spanContext));
  }

  @Test
  void inject_invalidContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    propagator.inject(
        withSpanContext(
            SpanContext.create(
                TraceId.getInvalid(),
                SpanId.getInvalid(),
                TraceFlags.getSampled(),
                TraceState.builder().put("foo", "bar").build()),
            Context.current()),
        carrier,
        setter);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject_SampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    propagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()),
            Context.current()),
        carrier,
        setter);
    assertThat(carrier).containsEntry(OtTracePropagator.TRACE_ID_HEADER, TRACE_ID_RIGHT_PART);
    assertThat(carrier).containsEntry(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    assertThat(carrier).containsEntry(OtTracePropagator.SAMPLED_HEADER, "true");
  }

  @Test
  void inject_SampledContext_nullCarrierUsage() {
    final Map<String, String> carrier = new LinkedHashMap<>();
    propagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()),
            Context.current()),
        null,
        (TextMapSetter<Map<String, String>>) (ignored, key, value) -> carrier.put(key, value));
    assertThat(carrier).containsEntry(OtTracePropagator.TRACE_ID_HEADER, TRACE_ID_RIGHT_PART);
    assertThat(carrier).containsEntry(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    assertThat(carrier).containsEntry(OtTracePropagator.SAMPLED_HEADER, "true");
  }

  @Test
  void inject_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    propagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()),
            Context.current()),
        carrier,
        setter);
    assertThat(carrier).containsEntry(OtTracePropagator.TRACE_ID_HEADER, TRACE_ID_RIGHT_PART);
    assertThat(carrier).containsEntry(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    assertThat(carrier).containsEntry(OtTracePropagator.SAMPLED_HEADER, "false");
  }

  @Test
  void inject_Baggage() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Baggage baggage = Baggage.builder().put("foo", "bar").put("key", "value").build();
    propagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()),
            Context.current().with(baggage)),
        carrier,
        setter);
    assertThat(carrier).containsEntry(OtTracePropagator.PREFIX_BAGGAGE_HEADER + "foo", "bar");
    assertThat(carrier).containsEntry(OtTracePropagator.PREFIX_BAGGAGE_HEADER + "key", "value");
  }

  @Test
  void inject_Baggage_InvalidContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Baggage baggage = Baggage.builder().put("foo", "bar").put("key", "value").build();
    propagator.inject(
        withSpanContext(
            SpanContext.create(
                TraceId.getInvalid(),
                SpanId.getInvalid(),
                TraceFlags.getSampled(),
                TraceState.getDefault()),
            Context.current().with(baggage)),
        carrier,
        setter);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject_nullContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    propagator.inject(null, carrier, setter);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject_nullSetter() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context =
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()),
            Context.current());
    propagator.inject(context, carrier, null);
    assertThat(carrier).isEmpty();
  }

  @Test
  void extract_Nothing() {
    // Context remains untouched.
    assertThat(
            propagator.extract(Context.current(), Collections.<String, String>emptyMap(), getter))
        .isSameAs(Context.current());
  }

  @Test
  void extract_SampledContext_Int() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracePropagator.TRACE_ID_HEADER, TRACE_ID);
    carrier.put(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(OtTracePropagator.SAMPLED_HEADER, Common.TRUE_INT);

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()));
  }

  @Test
  void extract_SampledContext_Bool() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracePropagator.TRACE_ID_HEADER, TRACE_ID);
    carrier.put(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(OtTracePropagator.SAMPLED_HEADER, "true");

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()));
  }

  @Test
  void extract_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracePropagator.TRACE_ID_HEADER, TRACE_ID);
    carrier.put(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(OtTracePropagator.SAMPLED_HEADER, Common.FALSE_INT);

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()));
  }

  @Test
  void extract_SampledContext_Int_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracePropagator.TRACE_ID_HEADER, SHORT_TRACE_ID);
    carrier.put(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(OtTracePropagator.SAMPLED_HEADER, Common.TRUE_INT);

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()));
  }

  @Test
  void extract_SampledContext_Bool_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracePropagator.TRACE_ID_HEADER, SHORT_TRACE_ID);
    carrier.put(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(OtTracePropagator.SAMPLED_HEADER, "true");

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()));
  }

  @Test
  void extract_NotSampledContext_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracePropagator.TRACE_ID_HEADER, SHORT_TRACE_ID);
    carrier.put(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(OtTracePropagator.SAMPLED_HEADER, Common.FALSE_INT);

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()));
  }

  @Test
  void extract_InvalidTraceId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(OtTracePropagator.TRACE_ID_HEADER, "abcdefghijklmnopabcdefghijklmnop");
    invalidHeaders.put(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    invalidHeaders.put(OtTracePropagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(OtTracePropagator.TRACE_ID_HEADER, TRACE_ID + "00");
    invalidHeaders.put(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    invalidHeaders.put(OtTracePropagator.SAMPLED_HEADER, Common.TRUE_INT);
    verifyInvalidBehavior(invalidHeaders);
  }

  @Test
  void extract_InvalidSpanId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(OtTracePropagator.TRACE_ID_HEADER, TRACE_ID);
    invalidHeaders.put(OtTracePropagator.SPAN_ID_HEADER, "abcdefghijklmnop");
    invalidHeaders.put(OtTracePropagator.SAMPLED_HEADER, Common.TRUE_INT);
    verifyInvalidBehavior(invalidHeaders);
  }

  @Test
  void extract_InvalidSpanId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(OtTracePropagator.TRACE_ID_HEADER, TRACE_ID);
    invalidHeaders.put(OtTracePropagator.SPAN_ID_HEADER, "abcdefghijklmnop" + "00");
    invalidHeaders.put(OtTracePropagator.SAMPLED_HEADER, Common.TRUE_INT);
    verifyInvalidBehavior(invalidHeaders);
  }

  private void verifyInvalidBehavior(Map<String, String> invalidHeaders) {
    Context input = Context.current();
    Context result = propagator.extract(input, invalidHeaders, getter);
    assertThat(result).isSameAs(input);
    assertThat(getSpanContext(result)).isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_emptyCarrier() {
    Map<String, String> emptyHeaders = new HashMap<>();
    verifyInvalidBehavior(emptyHeaders);
  }

  @Test
  void extract_Baggage() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracePropagator.TRACE_ID_HEADER, TRACE_ID);
    carrier.put(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(OtTracePropagator.SAMPLED_HEADER, Common.TRUE_INT);
    carrier.put(OtTracePropagator.PREFIX_BAGGAGE_HEADER + "foo", "bar");
    carrier.put(OtTracePropagator.PREFIX_BAGGAGE_HEADER + "key", "value");

    Context context = propagator.extract(Context.current(), carrier, getter);

    Baggage expectedBaggage = Baggage.builder().put("foo", "bar").put("key", "value").build();
    assertThat(Baggage.fromContext(context)).isEqualTo(expectedBaggage);
  }

  @Test
  void extract_Baggage_InvalidContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracePropagator.TRACE_ID_HEADER, TraceId.getInvalid());
    carrier.put(OtTracePropagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(OtTracePropagator.SAMPLED_HEADER, Common.TRUE_INT);
    carrier.put(OtTracePropagator.PREFIX_BAGGAGE_HEADER + "foo", "bar");
    carrier.put(OtTracePropagator.PREFIX_BAGGAGE_HEADER + "key", "value");

    Context context = propagator.extract(Context.current(), carrier, getter);

    assertThat(Baggage.fromContext(context).isEmpty()).isTrue();
  }

  @Test
  void extract_nullContext() {
    assertThat(propagator.extract(null, Collections.emptyMap(), getter)).isSameAs(Context.groot());
  }

  @Test
  void extract_nullGetter() {
    Context context =
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()),
            Context.current());
    assertThat(propagator.extract(context, Collections.emptyMap(), null)).isSameAs(context);
  }
}
