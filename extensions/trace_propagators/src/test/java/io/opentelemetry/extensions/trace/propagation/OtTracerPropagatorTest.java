/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.extensions.trace.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat.Getter;
import io.opentelemetry.context.propagation.HttpTextFormat.Setter;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OtTracerPropagatorTest {

  private static final TraceState TRACE_STATE_DEFAULT = TraceState.builder().build();
  private static final String TRACE_ID_BASE16 = "ff000000000000000000000000000041";
  private static final TraceId TRACE_ID = TraceId.fromLowerBase16(TRACE_ID_BASE16, 0);
  private static final String SHORT_TRACE_ID_BASE16 = "ff00000000000000";
  private static final TraceId SHORT_TRACE_ID =
      TraceId.fromLowerBase16(StringUtils.padLeft(SHORT_TRACE_ID_BASE16, 32), 0);
  private static final String SPAN_ID_BASE16 = "ff00000000000041";
  private static final SpanId SPAN_ID = SpanId.fromLowerBase16(SPAN_ID_BASE16, 0);
  private static final byte SAMPLED_TRACE_OPTIONS_BYTES = 1;
  private static final TraceFlags SAMPLED_TRACE_OPTIONS =
      TraceFlags.fromByte(SAMPLED_TRACE_OPTIONS_BYTES);
  private static final Setter<Map<String, String>> setter = Map::put;
  private static final Getter<Map<String, String>> getter = Map::get;
  private final OtTracerPropagator propagator = OtTracerPropagator.getInstance();

  private static SpanContext getSpanContext(Context context) {
    return TracingContextUtils.getSpan(context).getContext();
  }

  private static Context withSpanContext(SpanContext spanContext, Context context) {
    return TracingContextUtils.withSpan(DefaultSpan.create(spanContext), context);
  }

  @Test
  void inject_invalidContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    propagator.inject(
        withSpanContext(
            SpanContext.create(
                TraceId.getInvalid(),
                SpanId.getInvalid(),
                SAMPLED_TRACE_OPTIONS,
                TraceState.builder().set("foo", "bar").build()),
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
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            Context.current()),
        carrier,
        setter);
    assertThat(carrier).containsEntry(OtTracerPropagator.TRACE_ID_HEADER, TRACE_ID_BASE16);
    assertThat(carrier).containsEntry(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    assertThat(carrier).containsEntry(OtTracerPropagator.SAMPLED_HEADER, "true");
  }

  @Test
  void inject_SampledContext_nullCarrierUsage() {
    final Map<String, String> carrier = new LinkedHashMap<>();
    propagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            Context.current()),
        null,
        (Setter<Map<String, String>>) (ignored, key, value) -> carrier.put(key, value));
    assertThat(carrier).containsEntry(OtTracerPropagator.TRACE_ID_HEADER, TRACE_ID_BASE16);
    assertThat(carrier).containsEntry(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    assertThat(carrier).containsEntry(OtTracerPropagator.SAMPLED_HEADER, "true");
  }

  @Test
  void inject_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    propagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT),
            Context.current()),
        carrier,
        setter);
    assertThat(carrier).containsEntry(OtTracerPropagator.TRACE_ID_HEADER, TRACE_ID_BASE16);
    assertThat(carrier).containsEntry(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    assertThat(carrier).containsEntry(OtTracerPropagator.SAMPLED_HEADER, "false");
  }

  @Test
  void extract_Nothing() {
    // Context remains untouched.
    assertThat(
            propagator.extract(Context.current(), Collections.<String, String>emptyMap(), Map::get))
        .isSameAs(Context.current());
  }

  @Test
  void extract_SampledContext_Int() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracerPropagator.TRACE_ID_HEADER, TRACE_ID_BASE16);
    carrier.put(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    carrier.put(OtTracerPropagator.SAMPLED_HEADER, Common.TRUE_INT);

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Bool() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracerPropagator.TRACE_ID_HEADER, TRACE_ID_BASE16);
    carrier.put(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    carrier.put(OtTracerPropagator.SAMPLED_HEADER, "true");

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracerPropagator.TRACE_ID_HEADER, TRACE_ID_BASE16);
    carrier.put(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    carrier.put(OtTracerPropagator.SAMPLED_HEADER, Common.FALSE_INT);

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Int_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracerPropagator.TRACE_ID_HEADER, SHORT_TRACE_ID_BASE16);
    carrier.put(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    carrier.put(OtTracerPropagator.SAMPLED_HEADER, Common.TRUE_INT);

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_SampledContext_Bool_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracerPropagator.TRACE_ID_HEADER, SHORT_TRACE_ID_BASE16);
    carrier.put(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    carrier.put(OtTracerPropagator.SAMPLED_HEADER, "true");

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_NotSampledContext_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(OtTracerPropagator.TRACE_ID_HEADER, SHORT_TRACE_ID_BASE16);
    carrier.put(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    carrier.put(OtTracerPropagator.SAMPLED_HEADER, Common.FALSE_INT);

    assertThat(getSpanContext(propagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT));
  }

  @Test
  void extract_InvalidTraceId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(OtTracerPropagator.TRACE_ID_HEADER, "abcdefghijklmnopabcdefghijklmnop");
    invalidHeaders.put(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    invalidHeaders.put(OtTracerPropagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(OtTracerPropagator.TRACE_ID_HEADER, TRACE_ID_BASE16 + "00");
    invalidHeaders.put(OtTracerPropagator.SPAN_ID_HEADER, SPAN_ID_BASE16);
    invalidHeaders.put(OtTracerPropagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(OtTracerPropagator.TRACE_ID_HEADER, TRACE_ID_BASE16);
    invalidHeaders.put(OtTracerPropagator.SPAN_ID_HEADER, "abcdefghijklmnop");
    invalidHeaders.put(OtTracerPropagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(OtTracerPropagator.TRACE_ID_HEADER, TRACE_ID_BASE16);
    invalidHeaders.put(OtTracerPropagator.SPAN_ID_HEADER, "abcdefghijklmnop" + "00");
    invalidHeaders.put(OtTracerPropagator.SAMPLED_HEADER, Common.TRUE_INT);
    assertThat(getSpanContext(propagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_emptyCarrier() {
    Map<String, String> emptyHeaders = new HashMap<>();
    assertThat(getSpanContext(propagator.extract(Context.current(), emptyHeaders, getter)))
        .isEqualTo(SpanContext.getInvalid());
  }
}
