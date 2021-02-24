/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.aws;

import static io.opentelemetry.extension.aws.AwsXrayPropagator.TRACE_HEADER_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class AwsXrayPropagatorTest {

  private static final String TRACE_ID = "8a3c60f7d188f8fa79d48a391a778fa6";
  private static final String SPAN_ID = "53995c3f42cd8ad8";

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
  private final AwsXrayPropagator xrayPropagator = AwsXrayPropagator.getInstance();

  @Test
  void inject_SampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    xrayPropagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()),
            Context.current()),
        carrier,
        setter);

    assertThat(carrier)
        .containsEntry(
            TRACE_HEADER_KEY,
            "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=1");
  }

  @Test
  void inject_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    xrayPropagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()),
            Context.current()),
        carrier,
        setter);

    assertThat(carrier)
        .containsEntry(
            TRACE_HEADER_KEY,
            "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=0");
  }

  @Test
  void inject_WithTraceState() {
    Map<String, String> carrier = new LinkedHashMap<>();
    xrayPropagator.inject(
        withSpanContext(
            SpanContext.create(
                TRACE_ID,
                SPAN_ID,
                TraceFlags.getDefault(),
                TraceState.builder().put("foo", "bar").build()),
            Context.current()),
        carrier,
        setter);

    // TODO: assert trace state when the propagator supports it
    assertThat(carrier)
        .containsEntry(
            TRACE_HEADER_KEY,
            "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=0");
  }

  @Test
  void inject_nullContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    xrayPropagator.inject(null, carrier, setter);
    assertThat(carrier).isEmpty();
  }

  @Test
  void inject_nullSetter() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context =
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()),
            Context.current());
    xrayPropagator.inject(context, carrier, null);
    assertThat(carrier).isEmpty();
  }

  @Test
  void extract_Nothing() {
    // Context remains untouched.
    assertThat(
            xrayPropagator.extract(
                Context.current(), Collections.<String, String>emptyMap(), getter))
        .isSameAs(Context.current());
  }

  @Test
  void extract_SampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=1");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()));
  }

  @Test
  void extract_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=0");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()));
  }

  @Test
  void extract_DifferentPartOrder() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        TRACE_HEADER_KEY,
        "Parent=53995c3f42cd8ad8;Sampled=1;Root=1-8a3c60f7-d188f8fa79d48a391a778fa6");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()));
  }

  @Test
  void extract_AdditionalFields() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=1;Foo=Bar");

    // TODO: assert additional fields when the propagator supports it
    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()));
  }

  @Test
  void extract_EmptyHeaderValue() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(TRACE_HEADER_KEY, "");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=abcdefghijklmnopabcdefghijklmnop;Parent=53995c3f42cd8ad8;Sampled=0");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa600;Parent=53995c3f42cd8ad8;Sampled=0");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=abcdefghijklmnop;Sampled=0");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad800;Sampled=0");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidFlags() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidFlags_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=10220");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidFlags_NonNumeric() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=a");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_nullContext() {
    assertThat(xrayPropagator.extract(null, Collections.emptyMap(), getter))
        .isSameAs(Context.root());
  }

  @Test
  void extract_nullGetter() {
    Context context =
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()),
            Context.current());
    assertThat(xrayPropagator.extract(context, Collections.emptyMap(), null)).isSameAs(context);
  }

  private static Context withSpanContext(SpanContext spanContext, Context context) {
    return context.with(Span.wrap(spanContext));
  }

  private static SpanContext getSpanContext(Context context) {
    return Span.fromContext(context).getSpanContext();
  }
}
