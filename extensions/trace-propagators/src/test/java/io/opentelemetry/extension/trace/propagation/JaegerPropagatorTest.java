/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static io.opentelemetry.api.baggage.Baggage.fromContext;
import static io.opentelemetry.extension.trace.propagation.JaegerPropagator.BAGGAGE_HEADER;
import static io.opentelemetry.extension.trace.propagation.JaegerPropagator.BAGGAGE_PREFIX;
import static io.opentelemetry.extension.trace.propagation.JaegerPropagator.DEPRECATED_PARENT_SPAN;
import static io.opentelemetry.extension.trace.propagation.JaegerPropagator.PROPAGATION_HEADER;
import static io.opentelemetry.extension.trace.propagation.JaegerPropagator.PROPAGATION_HEADER_DELIMITER;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.jaegertracing.internal.JaegerSpanContext;
import io.jaegertracing.internal.propagation.TextMapCodec;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapPropagator.Setter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link io.opentelemetry.extension.trace.propagation.JaegerPropagator}. */
class JaegerPropagatorTest {

  private static final TraceState TRACE_STATE_DEFAULT = TraceState.builder().build();
  private static final long TRACE_ID_HI = 77L;
  private static final long TRACE_ID_LOW = 22L;
  private static final String TRACE_ID_BASE16 = "000000000000004d0000000000000016";
  private static final String TRACE_ID = TraceId.fromLongs(TRACE_ID_HI, TRACE_ID_LOW);
  private static final long SHORT_TRACE_ID_HI = 0L;
  private static final long SHORT_TRACE_ID_LOW = 2322222L;
  private static final String SHORT_TRACE_ID =
      TraceId.fromLongs(SHORT_TRACE_ID_HI, SHORT_TRACE_ID_LOW);
  private static final String SPAN_ID_BASE16 = "0000000000017c29";
  private static final long SPAN_ID_LONG = 97321L;
  private static final String SPAN_ID = SpanId.fromLong(SPAN_ID_LONG);
  private static final long DEPRECATED_PARENT_SPAN_LONG = 0L;
  private static final byte SAMPLED_TRACE_OPTIONS = TraceFlags.getSampled();
  private static final TextMapPropagator.Setter<Map<String, String>> setter = Map::put;
  private static final TextMapPropagator.Getter<Map<String, String>> getter =
      new TextMapPropagator.Getter<Map<String, String>>() {
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

  private final JaegerPropagator jaegerPropagator = JaegerPropagator.getInstance();

  private static SpanContext getSpanContext(Context context) {
    return Span.fromContext(context).getSpanContext();
  }

  private static Context withSpanContext(SpanContext spanContext, Context context) {
    return context.with(Span.wrap(spanContext));
  }

  @Test
  void inject_invalidContext() {
    Map<String, String> carrier = new LinkedHashMap<>();
    jaegerPropagator.inject(
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
  void inject_SampledContext_nullCarrierUsage() {
    final Map<String, String> carrier = new LinkedHashMap<>();

    jaegerPropagator.inject(
        withSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT),
            Context.current()),
        null,
        (Setter<Map<String, String>>) (ignored, key, value) -> carrier.put(key, value));

    assertThat(carrier)
        .containsEntry(
            PROPAGATION_HEADER,
            generateTraceIdHeaderValue(
                TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "1"));
  }

  @Test
  void inject_NotSampledContext() {
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
  void inject_SampledContext_withBaggage() {
    Map<String, String> carrier = new LinkedHashMap<>();
    Context context =
        Context.current()
            .with(
                Span.wrap(
                    SpanContext.create(
                        TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT)))
            .with(Baggage.builder().put("foo", "bar").build());

    jaegerPropagator.inject(context, carrier, setter);
    assertThat(carrier)
        .containsEntry(
            PROPAGATION_HEADER,
            generateTraceIdHeaderValue(
                TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "1"));
    assertThat(carrier).containsEntry(BAGGAGE_PREFIX + "foo", "bar");
  }

  @Test
  void inject_baggageOnly() {
    // Metadata won't be propagated, but it MUST NOT cause ay problem.
    Baggage baggage =
        Baggage.builder()
            .put("nometa", "nometa-value")
            .put("meta", "meta-value", BaggageEntryMetadata.create("somemetadata; someother=foo"))
            .build();
    Map<String, String> carrier = new LinkedHashMap<>();
    jaegerPropagator.inject(Context.root().with(baggage), carrier, Map::put);
    assertThat(carrier)
        .containsExactlyInAnyOrderEntriesOf(
            ImmutableMap.of(
                BAGGAGE_PREFIX + "nometa", "nometa-value",
                BAGGAGE_PREFIX + "meta", "meta-value"));
  }

  @Test
  void extract_Nothing() {
    // Context remains untouched.
    assertThat(
            jaegerPropagator.extract(
                Context.current(), Collections.<String, String>emptyMap(), getter))
        .isSameAs(Context.current());
  }

  @Test
  void extract_EmptyHeaderValue() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(PROPAGATION_HEADER, "");

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_NotEnoughParts() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(PROPAGATION_HEADER, "aa:bb:cc");

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_TooManyParts() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(PROPAGATION_HEADER, "aa:bb:cc:dd:ee");

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            "abcdefghijklmnopabcdefghijklmnop", SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "0"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidTraceId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            TRACE_ID_BASE16 + "00", SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "0"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            TRACE_ID_BASE16, "abcdefghijklmnop", DEPRECATED_PARENT_SPAN, "0"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidSpanId_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            TRACE_ID_BASE16, SPAN_ID_BASE16 + "00", DEPRECATED_PARENT_SPAN, "0"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidFlags() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, ""));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidFlags_Size() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "10220"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_InvalidFlags_NonNumeric() {
    Map<String, String> invalidHeaders = new LinkedHashMap<>();
    invalidHeaders.put(
        PROPAGATION_HEADER,
        generateTraceIdHeaderValue(
            TRACE_ID_BASE16, SPAN_ID_BASE16, DEPRECATED_PARENT_SPAN, "abcdefr"));

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), invalidHeaders, getter)))
        .isSameAs(SpanContext.getInvalid());
  }

  @Test
  void extract_SampledContext() {
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
  void extract_NotSampledContext() {
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
  void extract_SampledContext_Short_TraceId() {
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
  void extract_UrlEncodedContext() throws UnsupportedEncodingException {
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

  @Test
  void extract_SampledContext_withBaggage() {
    Map<String, String> carrier = new LinkedHashMap<>();
    JaegerSpanContext context =
        new JaegerSpanContext(
            TRACE_ID_HI, TRACE_ID_LOW, SPAN_ID_LONG, DEPRECATED_PARENT_SPAN_LONG, (byte) 5);
    carrier.put(PROPAGATION_HEADER, TextMapCodec.contextAsString(context));
    carrier.put(BAGGAGE_PREFIX + "foo", "bar");

    assertThat(getSpanContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                TRACE_ID, SPAN_ID, SAMPLED_TRACE_OPTIONS, TRACE_STATE_DEFAULT));
    assertThat(fromContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(Baggage.builder().put("foo", "bar").build());
  }

  @Test
  void extract_baggageOnly_withPrefix() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(BAGGAGE_PREFIX + "nometa", "nometa-value");
    carrier.put(BAGGAGE_PREFIX + "meta", "meta-value");
    carrier.put("another", "value");

    assertThat(fromContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            Baggage.builder().put("nometa", "nometa-value").put("meta", "meta-value").build());
  }

  @Test
  void extract_baggageOnly_withPrefix_emptyKey() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(BAGGAGE_PREFIX, "value"); // Not really a valid key.

    assertThat(fromContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(Baggage.empty());
  }

  @Test
  void extract_baggageOnly_withHeader() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(BAGGAGE_HEADER, "nometa=nometa-value,meta=meta-value");

    assertThat(fromContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            Baggage.builder().put("nometa", "nometa-value").put("meta", "meta-value").build());
  }

  @Test
  void extract_baggageOnly_withHeader_andSpaces() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(BAGGAGE_HEADER, "nometa = nometa-value , meta = meta-value");

    assertThat(fromContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            Baggage.builder().put("nometa", "nometa-value").put("meta", "meta-value").build());
  }

  @Test
  void extract_baggageOnly_withHeader_invalid() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(BAGGAGE_HEADER, "nometa+novalue");

    assertThat(fromContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(Baggage.empty());
  }

  @Test
  void extract_baggageOnly_withHeader_andPrefix() {
    Map<String, String> carrier = new LinkedHashMap<>();
    carrier.put(BAGGAGE_HEADER, "nometa=nometa-value,meta=meta-value");
    carrier.put(BAGGAGE_PREFIX + "foo", "bar");

    assertThat(fromContext(jaegerPropagator.extract(Context.current(), carrier, getter)))
        .isEqualTo(
            Baggage.builder()
                .put("nometa", "nometa-value")
                .put("meta", "meta-value")
                .put("foo", "bar")
                .build());
  }

  private static String generateTraceIdHeaderValue(
      String traceId, String spanId, char parentSpan, String sampled) {
    return traceId
        + PROPAGATION_HEADER_DELIMITER
        + spanId
        + PROPAGATION_HEADER_DELIMITER
        + parentSpan
        + PROPAGATION_HEADER_DELIMITER
        + sampled;
  }
}
