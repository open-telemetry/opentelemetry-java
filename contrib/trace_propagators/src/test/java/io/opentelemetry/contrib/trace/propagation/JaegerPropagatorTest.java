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

package io.opentelemetry.contrib.trace.propagation;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.contrib.trace.propagation.JaegerPropagator.DEPRECATED_PARENT_SPAN;
import static io.opentelemetry.contrib.trace.propagation.JaegerPropagator.PROPAGATION_HEADER;
import static io.opentelemetry.contrib.trace.propagation.JaegerPropagator.PROPAGATION_HEADER_DELIMITER;

import io.grpc.Context;
import io.jaegertracing.internal.JaegerSpanContext;
import io.jaegertracing.internal.propagation.TextMapCodec;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.context.propagation.HttpTextFormat.Setter;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.TracingContextUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opentelemetry.contrib.trace.propagation.JaegerPropagator}. */
@RunWith(JUnit4.class)
public class JaegerPropagatorTest {

  private static final TraceState TRACE_STATE_DEFAULT = TraceState.builder().build();
  private static final long TRACE_ID_HI = 77L;
  private static final long TRACE_ID_LOW = 22L;
  private static final String TRACE_ID_BASE16 = "000000000000004d0000000000000016";
  private static final TraceId TRACE_ID = new TraceId(TRACE_ID_HI, TRACE_ID_LOW);
  private static final long SHORT_TRACE_ID_HI = 0L;
  private static final long SHORT_TRACE_ID_LOW = 2322222L;
  private static final TraceId SHORT_TRACE_ID = new TraceId(SHORT_TRACE_ID_HI, SHORT_TRACE_ID_LOW);
  private static final String SPAN_ID_BASE16 = "0000000000017c29";
  private static final long SPAN_ID_LONG = 97321L;
  private static final SpanId SPAN_ID = new SpanId(SPAN_ID_LONG);
  private static final long DEPRECATED_PARENT_SPAN_LONG = 0L;
  private static final byte SAMPLED_TRACE_OPTIONS_BYTES = 1;
  private static final TraceFlags SAMPLED_TRACE_OPTIONS =
      TraceFlags.fromByte(SAMPLED_TRACE_OPTIONS_BYTES);
  private static final HttpTextFormat.Setter<Map<String, String>> setter =
      new HttpTextFormat.Setter<Map<String, String>>() {
        @Override
        public void set(Map<String, String> carrier, String key, String value) {
          carrier.put(key, value);
        }
      };
  private static final HttpTextFormat.Getter<Map<String, String>> getter =
      new HttpTextFormat.Getter<Map<String, String>>() {
        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  private final JaegerPropagator jaegerPropagator = new JaegerPropagator();

  @Rule public ExpectedException thrown = ExpectedException.none();

  private static SpanContext getSpanContext(Context context) {
    return TracingContextUtils.getSpan(context).getContext();
  }

  private static Context withSpanContext(SpanContext spanContext, Context context) {
    return TracingContextUtils.withSpan(DefaultSpan.create(spanContext), context);
  }

  @Test
  public void inject_SampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    jaegerPropagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            Context.current()),
        carrier,
        setter);

    assertThat(carrier)
        .containsEntry(
            PROPAGATION_HEADER,
            generateTraceIdHeaderValue(
                TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "1"));
  }

  @Test
  public void inject_SampledContext_nullCarrierUsage() {
    final Map<String, String> carrier = new LinkedHashMap<>();

    jaegerPropagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            Context.current()),
        null,
        new Setter<Map<String, String>>() {
          @Override
          public void set(Map<String, String> ignored, String key, String value) {
            carrier.put(key, value);
          }
        });

    assertThat(carrier)
        .containsEntry(
            PROPAGATION_HEADER,
            generateTraceIdHeaderValue(
                TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "1"));
  }

  @Test
  public void inject_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    jaegerPropagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT),
            Context.current()),
        carrier,
        setter);
    assertThat(carrier)
        .containsEntry(
            PROPAGATION_HEADER,
            generateTraceIdHeaderValue(
                TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "0"));
  }

  @Test
  public void extract_EmptyHeaderValue() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(PROPAGATION_HEADER, "");

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_NotEnoughParts() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(PROPAGATION_HEADER, "aa:bb:cc");

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_TooManyParts() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(PROPAGATION_HEADER, "aa:bb:cc:dd:ee");

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidTraceId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            "abcdefghijklmnopabcdefghijklmnop", SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "0"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidTraceId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            TRACE_ID_BASE16 + "00", SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "0"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidSpanId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            TRACE_ID_BASE16, "abcdefghijklmnop", DEPRECATED_PARENT_SPAN, "0"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidSpanId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            TRACE_ID_BASE16, SPAN_ID_BASE16 + "00", DEPRECATED_PARENT_SPAN, "0"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidFlags() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, ""));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidFlags_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "10220"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_InvalidFlags_NonNumeric() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "abcdefr"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameInstanceAs(SpanContext.getInvalid());
  }

  @Test
  public void extract_SampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    JaegerSpanContext context =
        new JaegerSpanContext(
            TRACE_ID_HI, TRACE_ID_LOW, SPAN_ID_LONG, DEPRECATED_PARENT_SPAN_LONG, (byte) 5);
    carrier.put(PROPAGATION_HEADER, TextMapCodec.contextAsString(context));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  public void extract_NotSampledContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    JaegerSpanContext context =
        new JaegerSpanContext(
            TRACE_ID_HI, TRACE_ID_LOW, SPAN_ID_LONG, DEPRECATED_PARENT_SPAN_LONG, (byte) 0);
    carrier.put(PROPAGATION_HEADER, TextMapCodec.contextAsString(context));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE_DEFAULT));
  }

  @Test
  public void extract_SampledContext_Short_TraceId() {
    Map<String, String> carrier = new LinkedHashMap<>();
    JaegerSpanContext context =
        new JaegerSpanContext(
            SHORT_TRACE_ID_HI,
            SHORT_TRACE_ID_LOW,
            SPAN_ID_LONG,
            DEPRECATED_PARENT_SPAN_LONG,
            (byte) 1);
    carrier.put(PROPAGATION_HEADER, TextMapCodec.contextAsString(context));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                SHORT_TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  @Test
  public void extract_UrlEncodedContext() throws UnsupportedEncodingException {
    Map<String, String> carrier = new LinkedHashMap<>();
    JaegerSpanContext context =
        new JaegerSpanContext(
            TRACE_ID_HI, TRACE_ID_LOW, SPAN_ID_LONG, DEPRECATED_PARENT_SPAN_LONG, (byte) 5);
    carrier.put(
        PROPAGATION_HEADER, URLEncoder.encode(TextMapCodec.contextAsString(context), "UTF-8"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
  }

  private static String generateTraceIdHeaderValue(
      String traceId, String spanId, char parentSpan, String sampled) {
    return new StringBuilder()
        .append(traceId)
        .append(PROPAGATION_HEADER_DELIMITER)
        .append(spanId)
        .append(PROPAGATION_HEADER_DELIMITER)
        .append(parentSpan)
        .append(PROPAGATION_HEADER_DELIMITER)
        .append(sampled)
        .toString();
  }
}
