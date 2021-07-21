/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.aws;

import static io.opentelemetry.extension.aws.AwsXrayPropagator.TRACE_HEADER_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.baggage.Baggage;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  void inject_WithBaggage() {
    Map<String, String> carrier = new LinkedHashMap<>();
    xrayPropagator.inject(
        withSpanContext(
                SpanContext.create(
                    TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()),
                Context.current())
            .with(
                Baggage.builder()
                    .put("cat", "meow")
                    .put("dog", "bark")
                    .put("Root", "ignored")
                    .put("Parent", "ignored")
                    .put("Sampled", "ignored")
                    .build()),
        carrier,
        setter);

    assertThat(carrier)
        .containsEntry(
            TRACE_HEADER_KEY,
            "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=0;"
                + "cat=meow;dog=bark");
  }

  @Test
  void inject_WithBaggage_LimitTruncates() {
    Map<String, String> carrier = new LinkedHashMap<>();
    // Limit is 256 characters for all baggage. We add a 254-character key/value pair and a
    // 3 character key value pair.
    String key1 = Stream.generate(() -> "a").limit(252).collect(Collectors.joining());
    String value1 = "a"; // 252 + 1 (=) + 1 = 254

    String key2 = "b";
    String value2 = "b"; // 1 + 1 (=) + 1 = 3

    Baggage baggage = Baggage.builder().put(key1, value1).put(key2, value2).build();

    xrayPropagator.inject(
        withSpanContext(
                SpanContext.create(
                    TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()),
                Context.current())
            .with(baggage),
        carrier,
        setter);

    assertThat(carrier)
        .containsEntry(
            TRACE_HEADER_KEY,
            "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=0;"
                + key1
                + '='
                + value1);
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

    // TODO: assert trace state when the propagator supports it, for general key/value pairs we are
    // mapping with baggage.
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

    Context context = xrayPropagator.extract(Context.current(), carrier, getter);
    assertThat(getSpanContext(context))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()));
    assertThat(Baggage.fromContext(context).getEntryValue("Foo")).isEqualTo("Bar");
  }

  @Test
  void extract_Baggage_LimitTruncates() {
    // Limit is 256 characters for all baggage. We add a 254-character key/value pair and a
    // 3 character key value pair.
    String key1 = Stream.generate(() -> "a").limit(252).collect(Collectors.joining());
    String value1 = "a"; // 252 + 1 (=) + 1 = 254

    String key2 = "b";
    String value2 = "b"; // 1 + 1 (=) + 1 = 3

    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=1;"
            + key1
            + '='
            + value1
            + ';'
            + key2
            + '='
            + value2);

    Context context = xrayPropagator.extract(Context.current(), carrier, getter);
    assertThat(getSpanContext(context))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()));
    assertThat(Baggage.fromContext(context).getEntryValue(key1)).isEqualTo(value1);
    assertThat(Baggage.fromContext(context).getEntryValue(key2)).isNull();
  }

  @Test
  void extract_EmptyHeaderValue() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(TRACE_HEADER_KEY, "");

    verifyInvalidBehavior(invalidHeaders);
  }

  @Test
  void extract_InvalidTraceId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=abcdefghijklmnopabcdefghijklmnop;Parent=53995c3f42cd8ad8;Sampled=0");

    verifyInvalidBehavior(invalidHeaders);
  }

  @Test
  void extract_InvalidTraceId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa600;Parent=53995c3f42cd8ad8;Sampled=0");

    verifyInvalidBehavior(invalidHeaders);
  }

  @Test
  void extract_InvalidSpanId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=abcdefghijklmnop;Sampled=0");

    verifyInvalidBehavior(invalidHeaders);
  }

  @Test
  void extract_InvalidSpanId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad800;Sampled=0");

    verifyInvalidBehavior(invalidHeaders);
  }

  @Test
  void extract_InvalidFlags() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=");

    verifyInvalidBehavior(invalidHeaders);
  }

  @Test
  void extract_InvalidFlags_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=10220");

    verifyInvalidBehavior(invalidHeaders);
  }

  @Test
  void extract_InvalidFlags_NonNumeric() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f7-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=a");

    verifyInvalidBehavior(invalidHeaders);
  }

  private void verifyInvalidBehavior(Map<String, String> invalidHeaders) {
    Context input = Context.current();
    Context result = xrayPropagator.extract(input, invalidHeaders, getter);
    assertThat(result).isSameAs(input);
    assertThat(getSpanContext(result)).isSameAs(SpanContext.getInvalid());
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

  @Test
  void extract_EpochPart_ZeroedSingleDigit() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        TRACE_HEADER_KEY,
        "Root=1-0-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=1;Foo=Bar");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                "00000000d188f8fa79d48a391a778fa6",
                SPAN_ID,
                TraceFlags.getSampled(),
                TraceState.getDefault()));
  }

  @Test
  void extract_EpochPart_TwoChars() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        TRACE_HEADER_KEY,
        "Root=1-1a-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=1;Foo=Bar");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                "0000001ad188f8fa79d48a391a778fa6",
                SPAN_ID,
                TraceFlags.getSampled(),
                TraceState.getDefault()));
  }

  @Test
  void extract_EpochPart_Zeroed() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        TRACE_HEADER_KEY,
        "Root=1-00000000-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=1;Foo=Bar");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                "00000000d188f8fa79d48a391a778fa6",
                SPAN_ID,
                TraceFlags.getSampled(),
                TraceState.getDefault()));
  }

  @Test
  void extract_InvalidTraceId_EpochPart_TooLong() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY,
        "Root=1-8a3c60f711-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=0");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_EpochPart_Empty() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY, "Root=1--d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=0");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_EpochPart_Missing() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_HEADER_KEY, "Root=1-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=0");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_WrongVersion() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        TRACE_HEADER_KEY,
        "Root=2-1a2a3a4a-d188f8fa79d48a391a778fa6;Parent=53995c3f42cd8ad8;Sampled=1;Foo=Bar");

    assertThat(getSpanContext(xrayPropagator.extract(Context.current(), carrier, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  private static Context withSpanContext(SpanContext spanContext, Context context) {
    return context.with(Span.wrap(spanContext));
  }

  private static SpanContext getSpanContext(Context context) {
    return Span.fromContext(context).getSpanContext();
  }
}
