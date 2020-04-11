/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.trace.propagation;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.trace.propagation.HttpTraceContext.TRACE_PARENT;
import static io.opentelemetry.trace.propagation.HttpTraceContext.TRACE_STATE;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opentelemetry.trace.propagation.HttpTraceContext}. */
@RunWith(JUnit4.class)
public class HttpTraceContextTest {

  private static final TraceState TRACE_STATE_DEFAULT = TraceState.builder().build();
  private static final TraceState TRACE_STATE_NOT_DEFAULT =
      TraceState.builder().set("foo", "bar").set("bar", "baz").build();
  private static final String TRACE_ID_BASE16 = "ff000000000000000000000000000041";
  private static final TraceId TRACE_ID = TraceId.fromLowerBase16(TRACE_ID_BASE16, 0);
  private static final String SPAN_ID_BASE16 = "ff00000000000041";
  private static final SpanId SPAN_ID = SpanId.fromLowerBase16(SPAN_ID_BASE16, 0);
  private static final byte SAMPLED_TRACE_OPTIONS_BYTES = 1;
  private static final TraceFlags SAMPLED_TRACE_OPTIONS =
      TraceFlags.fromByte(SAMPLED_TRACE_OPTIONS_BYTES);
  private static final String TRACEPARENT_HEADER_SAMPLED =
      "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-01";
  private static final String TRACEPARENT_HEADER_NOT_SAMPLED =
      "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-00";
  private static final Setter<Map<String, String>> setter =
      new Setter<Map<String, String>>() {
        @Override
        public void set(Map<String, String> carrier, String key, String value) {
          carrier.put(key, value);
        }
      };
  private static final Getter<Map<String, String>> getter =
      new Getter<Map<String, String>>() {
        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };
  // Encoding preserves the order which is the reverse order of adding.
  private static final String TRACESTATE_NOT_DEFAULT_ENCODING = "bar=baz,foo=bar";
  private static final String TRACESTATE_NOT_DEFAULT_ENCODING_WITH_SPACES =
      "bar=baz   ,    foo=bar";
  private final HttpTraceContext httpTraceContext = new HttpTraceContext();
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static SpanContext getSpanContext(Context context) {
    return TracingContextUtils.getSpan(context).getContext();
  }

  private static Context withSpanContext(SpanContext spanContext, Context context) {
    return TracingContextUtils.withSpan(DefaultSpan.create(spanContext), context);
  }

  @Test
  public void inject_Nothing() {
    Map<String, String> carrier = new LinkedHashMap<>();
    httpTraceContext.inject(Context.current(), carrier, setter);
    assertThat(carrier).hasSize(0);
  }

  @Test
  public void inject_NullCarrierUsage() {
    final Map<String, String> carrier = new LinkedHashMap<>();
    Context context =
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            Context.current());
    httpTraceContext.inject(
        context,
        null,
        new Setter<Map<String, String>>() {
          @Override
          public void set(Map<String, String> ignored, String key, String value) {
            carrier.put(key, value);
          }
        });
    assertThat(carrier).containsExactly(TRACE_PARENT, TRACEPARENT_HEADER_SAMPLED);
  }

  @Test
  public void inject_SampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context =
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            Context.current());
    httpTraceContext.inject(context, carrier, setter);
    assertThat(carrier).containsExactly(TRACE_PARENT, TRACEPARENT_HEADER_SAMPLED);
  }

  @Test
  public void inject_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context =
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT),
            Context.current());
    httpTraceContext.inject(context, carrier, setter);
    assertThat(carrier).containsExactly(TRACE_PARENT, TRACEPARENT_HEADER_NOT_SAMPLED);
  }

  @Test
  public void inject_SampledContext_WithTraceState() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context =
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_NOT_DEFAULT),
            Context.current());
    httpTraceContext.inject(context, carrier, setter);
    assertThat(carrier)
        .containsExactly(
            TRACE_PARENT, TRACEPARENT_HEADER_SAMPLED, TRACE_STATE, TRACESTATE_NOT_DEFAULT_ENCODING);
  }

  @Test
  public void inject_NotSampledContext_WithTraceState() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context =
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_NOT_DEFAULT),
            Context.current());
    httpTraceContext.inject(context, carrier, setter);
    assertThat(carrier)
        .containsExactly(
            TRACE_PARENT,
            TRACEPARENT_HEADER_NOT_SAMPLED,
            TRACE_STATE,
            TRACESTATE_NOT_DEFAULT_ENCODING);
  }

  @Test
  public void extract_SampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(TRACE_PARENT, TRACEPARENT_HEADER_SAMPLED);
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(TRACE_PARENT, TRACEPARENT_HEADER_NOT_SAMPLED);
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT));
  }

  @Test
  public void extract_SampledContext_WithTraceState() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(TRACE_PARENT, TRACEPARENT_HEADER_SAMPLED);
    carrier.put(TRACE_STATE, TRACESTATE_NOT_DEFAULT_ENCODING);
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_NOT_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext_WithTraceState() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(TRACE_PARENT, TRACEPARENT_HEADER_NOT_SAMPLED);
    carrier.put(TRACE_STATE, TRACESTATE_NOT_DEFAULT_ENCODING);
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_NOT_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext_NextVersion() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(TRACE_PARENT, "01-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-00-02");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext_EmptyTraceState() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(TRACE_PARENT, TRACEPARENT_HEADER_NOT_SAMPLED);
    carrier.put(TRACE_STATE, "");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext_TraceStateWithSpaces() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(TRACE_PARENT, TRACEPARENT_HEADER_NOT_SAMPLED);
    carrier.put(TRACE_STATE, TRACESTATE_NOT_DEFAULT_ENCODING_WITH_SPACES);
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_NOT_DEFAULT));
  }

  @Test
  public void extract_EmptyHeader() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(TRACE_PARENT, "");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidTraceId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        TRACE_PARENT, "00-" + "abcdefghijklmnopabcdefghijklmnop" + "-" + SPAN_ID_BASE16 + "-01");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidTraceId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(TRACE_PARENT, "00-" + TRACE_ID_BASE16 + "00-" + SPAN_ID_BASE16 + "-01");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidSpanId() {
    Map<String, String> invalidHeaders = new HashMap<String, String>();
    invalidHeaders.put(TRACE_PARENT, "00-" + TRACE_ID_BASE16 + "-" + "abcdefghijklmnop" + "-01");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidSpanId_Size() {
    Map<String, String> invalidHeaders = new HashMap<>();
    invalidHeaders.put(TRACE_PARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "00-01");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidTraceFlags() {
    Map<String, String> invalidHeaders = new HashMap<>();
    invalidHeaders.put(TRACE_PARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-gh");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidTraceFlags_Size() {
    Map<String, String> invalidHeaders = new HashMap<>();
    invalidHeaders.put(TRACE_PARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-0100");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidTracestate_EntriesDelimiter() {
    Map<String, String> invalidHeaders = new HashMap<>();
    invalidHeaders.put(TRACE_PARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-01");
    invalidHeaders.put(TRACE_STATE, "foo=bar;test=test");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), invalidHeaders, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  public void extract_InvalidTracestate_KeyValueDelimiter() {
    Map<String, String> invalidHeaders = new HashMap<>();
    invalidHeaders.put(TRACE_PARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-01");
    invalidHeaders.put(TRACE_STATE, "foo=bar,test-test");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), invalidHeaders, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  public void extract_InvalidTracestate_OneString() {
    Map<String, String> invalidHeaders = new HashMap<>();
    invalidHeaders.put(TRACE_PARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-01");
    invalidHeaders.put(TRACE_STATE, "test-test");
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), invalidHeaders, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  public void fieldsList() {
    assertThat(httpTraceContext.fields()).containsExactly(TRACE_PARENT, TRACE_STATE);
  }

  @Test
  public void headerNames() {
    assertThat(TRACE_PARENT).isEqualTo("traceparent");
    assertThat(TRACE_STATE).isEqualTo("tracestate");
  }

  @Test
  public void extract_emptyCarrier() {
    Map<String, String> emptyHeaders = new HashMap<>();
    assertThat(getSpanContext(httpTraceContext.extract(Context.current(), emptyHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }
}
