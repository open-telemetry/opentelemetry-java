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

package io.opentelemetry.context.propagation;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.context.propagation.TraceContextFormat.TRACEPARENT;
import static io.opentelemetry.context.propagation.TraceContextFormat.TRACESTATE;

import io.opentelemetry.context.propagation.HttpTextFormat.Getter;
import io.opentelemetry.context.propagation.HttpTextFormat.Setter;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceOptions;
import io.opentelemetry.trace.Tracestate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceContextFormat}. */
@RunWith(JUnit4.class)
public class TraceContextFormatTest {

  private static final Tracestate TRACESTATE_DEFAULT = Tracestate.builder().build();
  private static final Tracestate TRACESTATE_NOT_DEFAULT =
      Tracestate.builder().set("foo", "bar").set("bar", "baz").build();
  private static final String TRACE_ID_BASE16 = "ff000000000000000000000000000041";
  private static final TraceId TRACE_ID = TraceId.fromLowerBase16(TRACE_ID_BASE16, 0);
  private static final String SPAN_ID_BASE16 = "ff00000000000041";
  private static final SpanId SPAN_ID = SpanId.fromLowerBase16(SPAN_ID_BASE16, 0);
  private static final byte SAMPLED_TRACE_OPTIONS_BYTES = 1;
  private static final TraceOptions SAMPLED_TRACE_OPTIONS =
      TraceOptions.fromByte(SAMPLED_TRACE_OPTIONS_BYTES);
  private static final String TRACEPARENT_HEADER_SAMPLED =
      "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-01";
  private static final String TRACEPARENT_HEADER_NOT_SAMPLED =
      "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-00";
  private static final Setter<Map<String, String>> setter =
      new Setter<Map<String, String>>() {
        @Override
        public void put(Map<String, String> carrier, String key, String value) {
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
  private final TraceContextFormat traceContextFormat = new TraceContextFormat();
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void inject_SampledContext() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    traceContextFormat.inject(
        SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACESTATE_DEFAULT),
        carrier,
        setter);
    assertThat(carrier).containsExactly(TRACEPARENT, TRACEPARENT_HEADER_SAMPLED);
  }

  @Test
  public void inject_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    traceContextFormat.inject(
        SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT, TRACESTATE_DEFAULT),
        carrier,
        setter);
    assertThat(carrier).containsExactly(TRACEPARENT, TRACEPARENT_HEADER_NOT_SAMPLED);
  }

  @Test
  public void inject_SampledContext_WithTraceState() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    traceContextFormat.inject(
        SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACESTATE_NOT_DEFAULT),
        carrier,
        setter);
    assertThat(carrier)
        .containsExactly(
            TRACEPARENT, TRACEPARENT_HEADER_SAMPLED, TRACESTATE, TRACESTATE_NOT_DEFAULT_ENCODING);
  }

  @Test
  public void inject_NotSampledContext_WithTraceState() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    traceContextFormat.inject(
        SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT, TRACESTATE_NOT_DEFAULT),
        carrier,
        setter);
    assertThat(carrier)
        .containsExactly(
            TRACEPARENT,
            TRACEPARENT_HEADER_NOT_SAMPLED,
            TRACESTATE,
            TRACESTATE_NOT_DEFAULT_ENCODING);
  }

  @Test
  public void extract_SampledContext() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    carrier.put(TRACEPARENT, TRACEPARENT_HEADER_SAMPLED);
    assertThat(traceContextFormat.extract(carrier, getter))
        .isEqualTo(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACESTATE_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    carrier.put(TRACEPARENT, TRACEPARENT_HEADER_NOT_SAMPLED);
    assertThat(traceContextFormat.extract(carrier, getter))
        .isEqualTo(SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT, TRACESTATE_DEFAULT));
  }

  @Test
  public void extract_SampledContext_WithTraceState() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    carrier.put(TRACEPARENT, TRACEPARENT_HEADER_SAMPLED);
    carrier.put(TRACESTATE, TRACESTATE_NOT_DEFAULT_ENCODING);
    assertThat(traceContextFormat.extract(carrier, getter))
        .isEqualTo(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACESTATE_NOT_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext_WithTraceState() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    carrier.put(TRACEPARENT, TRACEPARENT_HEADER_NOT_SAMPLED);
    carrier.put(TRACESTATE, TRACESTATE_NOT_DEFAULT_ENCODING);
    assertThat(traceContextFormat.extract(carrier, getter))
        .isEqualTo(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT, TRACESTATE_NOT_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext_NextVersion() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    carrier.put(TRACEPARENT, "01-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-00-02");
    assertThat(traceContextFormat.extract(carrier, getter))
        .isEqualTo(SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT, TRACESTATE_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext_EmptyTraceState() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    carrier.put(TRACEPARENT, TRACEPARENT_HEADER_NOT_SAMPLED);
    carrier.put(TRACESTATE, "");
    assertThat(traceContextFormat.extract(carrier, getter))
        .isEqualTo(SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT, TRACESTATE_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext_TraceStateWithSpaces() {
    Map<String, String> carrier = new LinkedHashMap<String, String>();
    carrier.put(TRACEPARENT, TRACEPARENT_HEADER_NOT_SAMPLED);
    carrier.put(TRACESTATE, "foo=bar   ,    bar=baz");
    assertThat(traceContextFormat.extract(carrier, getter))
        .isEqualTo(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceOptions.DEFAULT, TRACESTATE_NOT_DEFAULT));
  }

  @Test
  public void extract_InvalidTraceId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<String, String>();
    invalidHeaders.put(
        TRACEPARENT, "00-" + "abcdefghijklmnopabcdefghijklmnop" + "-" + SPAN_ID_BASE16 + "-01");
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Invalid traceparent: "
            + "00-"
            + "abcdefghijklmnopabcdefghijklmnop"
            + "-"
            + SPAN_ID_BASE16
            + "-01");
    traceContextFormat.extract(invalidHeaders, getter);
  }

  @Test
  public void extract_InvalidTraceId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<String, String>();
    invalidHeaders.put(TRACEPARENT, "00-" + TRACE_ID_BASE16 + "00-" + SPAN_ID_BASE16 + "-01");
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Invalid traceparent: " + "00-" + TRACE_ID_BASE16 + "00-" + SPAN_ID_BASE16 + "-01");
    traceContextFormat.extract(invalidHeaders, getter);
  }

  @Test
  public void extract_InvalidSpanId() {
    Map<String, String> invalidHeaders = new HashMap<String, String>();
    invalidHeaders.put(TRACEPARENT, "00-" + TRACE_ID_BASE16 + "-" + "abcdefghijklmnop" + "-01");
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Invalid traceparent: " + "00-" + TRACE_ID_BASE16 + "-" + "abcdefghijklmnop" + "-01");
    traceContextFormat.extract(invalidHeaders, getter);
  }

  @Test
  public void extract_InvalidSpanId_Size() {
    Map<String, String> invalidHeaders = new HashMap<String, String>();
    invalidHeaders.put(TRACEPARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "00-01");
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Invalid traceparent: " + "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "00-01");
    traceContextFormat.extract(invalidHeaders, getter);
  }

  @Test
  public void extract_InvalidTraceOptions() {
    Map<String, String> invalidHeaders = new HashMap<String, String>();
    invalidHeaders.put(TRACEPARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-gh");
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Invalid traceparent: " + "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-gh");
    traceContextFormat.extract(invalidHeaders, getter);
  }

  @Test
  public void extract_InvalidTraceOptions_Size() {
    Map<String, String> invalidHeaders = new HashMap<String, String>();
    invalidHeaders.put(TRACEPARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-0100");
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Invalid traceparent: " + "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-0100");
    traceContextFormat.extract(invalidHeaders, getter);
  }

  @Test
  public void extract_InvalidTracestate_EntriesDelimiter() {
    Map<String, String> invalidHeaders = new HashMap<String, String>();
    invalidHeaders.put(TRACEPARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-01");
    invalidHeaders.put(TRACESTATE, "foo=bar;test=test");
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid tracestate: " + "foo=bar;test=test");
    traceContextFormat.extract(invalidHeaders, getter);
  }

  @Test
  public void extract_InvalidTracestate_KeyValueDelimiter() {
    Map<String, String> invalidHeaders = new HashMap<String, String>();
    invalidHeaders.put(TRACEPARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-01");
    invalidHeaders.put(TRACESTATE, "foo=bar,test-test");
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid tracestate: " + "foo=bar,test-test");
    traceContextFormat.extract(invalidHeaders, getter);
  }

  @Test
  public void extract_InvalidTracestate_OneString() {
    Map<String, String> invalidHeaders = new HashMap<String, String>();
    invalidHeaders.put(TRACEPARENT, "00-" + TRACE_ID_BASE16 + "-" + SPAN_ID_BASE16 + "-01");
    invalidHeaders.put(TRACESTATE, "test-test");
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid tracestate: " + "test-test");
    traceContextFormat.extract(invalidHeaders, getter);
  }

  @Test
  public void fieldsList() {
    assertThat(traceContextFormat.fields()).containsExactly(TRACEPARENT, TRACESTATE);
  }

  @Test
  public void headerNames() {
    assertThat(TRACEPARENT).isEqualTo("traceparent");
    assertThat(TRACESTATE).isEqualTo("tracestate");
  }
}
