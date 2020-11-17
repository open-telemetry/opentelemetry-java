/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static io.opentelemetry.extension.trace.propagation.B3Propagator.DEBUG_CONTEXT_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator.Getter;
import io.opentelemetry.context.propagation.TextMapPropagator.Setter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link io.opentelemetry.api.trace.propagation.HttpTraceContext}. */
class B3PropagatorTest {

  private static final TraceState TRACE_STATE_DEFAULT = TraceState.builder().build();
  private static final String TRACE_ID = "ff000000000000000000000000000041";
  private static final String EXTRA_TRACE_ID = "ff000000000000000000000000000045";
  private static final String TRACE_ID_ALL_ZERO = "00000000000000000000000000000000";
  private static final String SHORT_TRACE_ID = "ff00000000000000";
  private static final String SHORT_TRACE_ID_FULL = StringUtils.padLeft(SHORT_TRACE_ID, 32);
  private static final String SPAN_ID = "ff00000000000041";
  private static final String EXTRA_SPAN_ID = "ff00000000000045";
  private static final String SPAN_ID_ALL_ZERO = "0000000000000000";
  private static final byte SAMPLED_TRACE_OPTIONS = TraceFlags.getSampled();
  private static final Setter<Map<String, String>> setter = Map::put;
  private static final Getter<Map<String, String>> getter =
      new Getter<Map<String, String>>() {
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
  private final B3Propagator b3Propagator = B3Propagator.builder().injectMultipleHeaders().build();
  private final B3Propagator b3PropagatorSingleHeader = B3Propagator.getInstance();

  private static SpanContext getSpanContext(Context context) {
    return Span.fromContext(context).getSpanContext();
  }

  private static Context withSpanContext(SpanContext spanContext, Context context) {
    return context.with(Span.wrap(spanContext));
  }

  @Test
  void inject_invalidContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    b3Propagator.inject(
        withSpanContext(
            SpanContext.create(
                TraceId.getInvalid(),
                SpanId.getInvalid(),
                SAMPLED_TRACE_OPTIONS,
                TraceState.builder().set("foo", "bar").build()),
            Context.current()),
        carrier,
        setter);
    assertThat(carrier).hasSize(0);
  }

  @Test
  void inject_SampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    b3Propagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            Context.current()),
        carrier,
        setter);
    assertThat(carrier).containsEntry(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    assertThat(carrier).containsEntry(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    assertThat(carrier).containsEntry(B3Propagator.SAMPLED_HEADER, "1");
  }

  @Test
  void inject_SampledContext_nullCarrierUsage() {
    final Map<String, String> carrier = new LinkedHashMap<>();
    b3Propagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            Context.current()),
        null,
        (Setter<Map<String, String>>) (ignored, key, value) -> carrier.put(key, value));
    assertThat(carrier).containsEntry(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    assertThat(carrier).containsEntry(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    assertThat(carrier).containsEntry(B3Propagator.SAMPLED_HEADER, "1");
  }

  @Test
  void inject_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    b3Propagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT),
            Context.current()),
        carrier,
        setter);
    assertThat(carrier).containsEntry(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    assertThat(carrier).containsEntry(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    assertThat(carrier).containsEntry(B3Propagator.SAMPLED_HEADER, "0");
  }

  @Test
  void extract_Nothing() {
    // Context remains untouched.
    assertThat(
            b3Propagator.extract(Context.current(), Collections.<String, String>emptyMap(), getter))
        .isSameAs(Context.current());
  }

  @Test
  void extract_SampledContext_Int() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Bool() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(B3Propagator.SAMPLED_HEADER, "true");

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(B3Propagator.SAMPLED_HEADER, Common.FALSE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Int_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.TRACE_ID_HEADER, SHORT_TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Bool_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.TRACE_ID_HEADER, SHORT_TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(B3Propagator.SAMPLED_HEADER, "true");

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_NotSampledContext_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.TRACE_ID_HEADER, SHORT_TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(B3Propagator.SAMPLED_HEADER, Common.FALSE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_InvalidTraceId_NotHex() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.TRACE_ID_HEADER, "g" + TRACE_ID.substring(1));
    invalidHeaders.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    invalidHeaders.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_TooShort() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID.substring(2));
    invalidHeaders.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    invalidHeaders.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_TooLong() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID + "00");
    invalidHeaders.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    invalidHeaders.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_AllZero() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID_ALL_ZERO);
    invalidHeaders.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    invalidHeaders.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId_NotHex() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    invalidHeaders.put(B3Propagator.SPAN_ID_HEADER, "g" + SPAN_ID.substring(1));
    invalidHeaders.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId_TooShort() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    invalidHeaders.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID.substring(2));
    invalidHeaders.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId_TooLong() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    invalidHeaders.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID + "00");
    invalidHeaders.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId_AllZeros() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    invalidHeaders.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID_ALL_ZERO);
    invalidHeaders.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void inject_invalidContext_SingleHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    b3PropagatorSingleHeader.inject(
        withSpanContext(
            SpanContext.create(
                TraceId.getInvalid(),
                SpanId.getInvalid(),
                SAMPLED_TRACE_OPTIONS,
                TraceState.builder().set("foo", "bar").build()),
            Context.current()),
        carrier,
        setter);
    assertThat(carrier).hasSize(0);
  }

  @Test
  void inject_SampledContext_SingleHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    b3PropagatorSingleHeader.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            Context.current()),
        carrier,
        setter);

    assertThat(carrier)
        .containsEntry(B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + SPAN_ID + "-" + "1");
  }

  @Test
  void inject_NotSampledContext_SingleHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    b3PropagatorSingleHeader.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT),
            Context.current()),
        carrier,
        setter);

    assertThat(carrier)
        .containsEntry(B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + SPAN_ID + "-" + "0");
  }

  @Test
  void extract_Nothing_SingleHeader() {
    // Context remains untouched.
    assertThat(
            b3Propagator.extract(Context.current(), Collections.<String, String>emptyMap(), getter))
        .isSameAs(Context.current());
  }

  @Test
  void extract_SampledContext_Int_SingleHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + SPAN_ID + "-" + Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_DebugFlag_SingleHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + SPAN_ID + "-" + Common.TRUE_INT + "-" + "0");

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Bool_SingleHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + SPAN_ID + "-" + "true");

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Bool_DebugFlag_SingleHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + SPAN_ID + "-" + "true" + "-" + "0");

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_NotSampledContext_SingleHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + SPAN_ID + "-" + Common.FALSE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Int_SingleHeader_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        B3Propagator.COMBINED_HEADER, SHORT_TRACE_ID + "-" + SPAN_ID + "-" + Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_DebugFlag_SingleHeader_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        B3Propagator.COMBINED_HEADER,
        SHORT_TRACE_ID + "-" + SPAN_ID + "-" + Common.TRUE_INT + "-" + "0");

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Bool_SingleHeader_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.COMBINED_HEADER, SHORT_TRACE_ID + "-" + SPAN_ID + "-" + "true");

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Bool_DebugFlag_SingleHeader_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        B3Propagator.COMBINED_HEADER, SHORT_TRACE_ID + "-" + SPAN_ID + "-" + "true" + "-" + "0");

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_NotSampledContext_SingleHeader_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        B3Propagator.COMBINED_HEADER, SHORT_TRACE_ID + "-" + SPAN_ID + "-" + Common.FALSE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID_FULL, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_Null_SingleHeader() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.COMBINED_HEADER, null);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_Empty_SingleHeader() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.COMBINED_HEADER, "");

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_SingleHeader() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        B3Propagator.COMBINED_HEADER,
        "abcdefghijklmnopabcdefghijklmnop" + "-" + SPAN_ID + "-" + Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_Size_SingleHeader() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        B3Propagator.COMBINED_HEADER,
        "abcdefghijklmnopabcdefghijklmnop" + "00" + "-" + SPAN_ID + "-" + Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId_SingleHeader() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + "abcdefghijklmnop" + "-" + Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId_Size_SingleHeader() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        B3Propagator.COMBINED_HEADER,
        TRACE_ID + "-" + "abcdefghijklmnop" + "00" + "-" + Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_TooFewParts_SingleHeader() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(B3Propagator.COMBINED_HEADER, TRACE_ID);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_TooManyParts_SingleHeader() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        B3Propagator.COMBINED_HEADER,
        TRACE_ID + "-" + SPAN_ID + "-" + Common.TRUE_INT + "-extra-extra");
    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_SampledContext_Int_From_SingleHeader_When_MultipleHeadersAlsoPresent() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + SPAN_ID + "-" + Common.TRUE_INT);
    carrier.put(B3Propagator.TRACE_ID_HEADER, EXTRA_TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, EXTRA_SPAN_ID);
    carrier.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Int_From_MultipleHeaders_When_InvalidSingleHeaderProvided() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(
        B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + "abcdefghijklmnop" + "-" + Common.TRUE_INT);
    carrier.put(B3Propagator.TRACE_ID_HEADER, EXTRA_TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, EXTRA_SPAN_ID);
    carrier.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                EXTRA_TRACE_ID, EXTRA_SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_Invalid_When_Invalid_Single_And_MultipleHeaders_Provided() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + "abcdefghijklmnop" + "-" + Common.TRUE_INT);
    invalidHeaders.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    invalidHeaders.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID + "00");
    invalidHeaders.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);

    assertThat(getSpanContext(b3Propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void fieldsList() {
    assertThat(b3Propagator.fields())
        .containsExactly(
            B3Propagator.TRACE_ID_HEADER,
            B3Propagator.SPAN_ID_HEADER,
            B3Propagator.SAMPLED_HEADER,
            B3Propagator.COMBINED_HEADER);
  }

  @Test
  void headerNames() {
    assertThat(B3Propagator.TRACE_ID_HEADER).isEqualTo("X-B3-TraceId");
    assertThat(B3Propagator.SPAN_ID_HEADER).isEqualTo("X-B3-SpanId");
    assertThat(B3Propagator.SAMPLED_HEADER).isEqualTo("X-B3-Sampled");
    assertThat(B3Propagator.COMBINED_HEADER).isEqualTo("b3");
  }

  @Test
  void extract_emptyCarrier() {
    Map<String, String> emptyHeaders = new HashMap<>();
    assertThat(getSpanContext(b3Propagator.extract(Context.current(), emptyHeaders, getter)))
        .isEqualTo(SpanContext.getInvalid());
  }

  @Test
  void extract_DebugContext_SingleHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.COMBINED_HEADER, TRACE_ID + "-" + SPAN_ID + "-" + "d");

    Context context = b3Propagator.extract(Context.current(), carrier, getter);
    assertThat(getSpanContext(context))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
    assertTrue(context.get(DEBUG_CONTEXT_KEY));
  }

  @Test
  void extract_DebugContext_MultipleHeaders() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(B3Propagator.DEBUG_HEADER, Common.TRUE_INT);

    Context context = b3Propagator.extract(Context.current(), carrier, getter);
    assertThat(getSpanContext(context))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
    assertTrue(context.get(DEBUG_CONTEXT_KEY));
  }

  @Test
  void extract_DebugContext_SampledFalseDebugTrue_MultipleHeaders() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(B3Propagator.SAMPLED_HEADER, Common.FALSE_INT);
    carrier.put(B3Propagator.DEBUG_HEADER, Common.TRUE_INT);

    Context context = b3Propagator.extract(Context.current(), carrier, getter);
    assertThat(getSpanContext(context))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
    assertTrue(context.get(DEBUG_CONTEXT_KEY));
  }

  @Test
  void extract_DebugContext_SampledTrueDebugTrue_MultipleHeaders() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    carrier.put(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    carrier.put(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);
    carrier.put(B3Propagator.DEBUG_HEADER, Common.TRUE_INT);

    Context context = b3Propagator.extract(Context.current(), carrier, getter);
    assertThat(getSpanContext(context))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
    assertTrue(context.get(DEBUG_CONTEXT_KEY));
  }

  @Test
  void inject_DebugContext_MultipleHeaders() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context = Context.current().with(DEBUG_CONTEXT_KEY, true);
    b3Propagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            context),
        carrier,
        setter);
    assertThat(carrier).containsEntry(B3Propagator.TRACE_ID_HEADER, TRACE_ID);
    assertThat(carrier).containsEntry(B3Propagator.SPAN_ID_HEADER, SPAN_ID);
    assertThat(carrier).containsEntry(B3Propagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(carrier).containsEntry(B3Propagator.DEBUG_HEADER, Common.TRUE_INT);
  }

  @Test
  void inject_DebugContext_SingleHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context = Context.current().with(DEBUG_CONTEXT_KEY, true);
    b3PropagatorSingleHeader.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            context),
        carrier,
        setter);
    assertThat(carrier)
        .containsEntry(
            B3Propagator.COMBINED_HEADER,
            TRACE_ID + "-" + SPAN_ID + "-" + B3Propagator.SINGLE_HEADER_DEBUG);
  }
}
