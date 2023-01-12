/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.opentracingshim.TestUtils.getBaggageMap;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpanShimTest {

  private final SdkTracerProvider tracerSdkFactory = SdkTracerProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("SpanShimTest");
  private Span span;

  private static final String SPAN_NAME = "Span";

  @BeforeEach
  void setUp() {
    span = tracer.spanBuilder(SPAN_NAME).startSpan();
  }

  @AfterEach
  void tearDown() {
    span.end();
  }

  @Test
  void context_simple() {
    SpanShim spanShim = new SpanShim(span);

    SpanContextShim contextShim = (SpanContextShim) spanShim.context();
    assertThat(contextShim).isNotNull();
    assertThat(span.getSpanContext()).isEqualTo(contextShim.getSpanContext());
    assertThat(span.getSpanContext().getTraceId()).isEqualTo(contextShim.toTraceId());
    assertThat(span.getSpanContext().getSpanId()).isEqualTo(contextShim.toSpanId());
    assertThat(contextShim.baggageItems().iterator().hasNext()).isFalse();
  }

  @Test
  void setAttribute_errorAsBoolean() {
    SpanShim spanShim = new SpanShim(span);
    spanShim.setTag(Tags.ERROR.getKey(), true);

    SpanData spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getStatus()).isEqualTo(StatusData.error());

    spanShim.setTag(Tags.ERROR.getKey(), false);
    spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getStatus()).isEqualTo(StatusData.ok());
  }

  @Test
  void setAttribute_errorAsString() {
    SpanShim spanShim = new SpanShim(span);
    spanShim.setTag(Tags.ERROR.getKey(), "tRuE");

    SpanData spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getStatus()).isEqualTo(StatusData.error());

    spanShim.setTag(Tags.ERROR.getKey(), "FaLsE");
    spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getStatus()).isEqualTo(StatusData.ok());
  }

  @Test
  void setAttribute_unrecognizedType() {
    SpanShim spanShim = new SpanShim(span);
    spanShim.setTag("foo", BigInteger.ONE);

    SpanData spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getAttributes().size()).isEqualTo(1);
    assertThat(spanData.getAttributes().get(AttributeKey.stringKey("foo"))).isEqualTo("1");
  }

  @Test
  void baggage() {
    SpanShim spanShim = new SpanShim(span);

    spanShim.setBaggageItem("key1", "value1");
    spanShim.setBaggageItem("key2", "value2");
    assertThat("value1").isEqualTo(spanShim.getBaggageItem("key1"));
    assertThat("value2").isEqualTo(spanShim.getBaggageItem("key2"));

    SpanContextShim contextShim = (SpanContextShim) spanShim.context();
    assertThat(contextShim).isNotNull();
    Map<String, String> baggageMap = getBaggageMap(contextShim.baggageItems());
    assertThat(baggageMap.size()).isEqualTo(2);
    assertThat("value1").isEqualTo(baggageMap.get("key1"));
    assertThat("value2").isEqualTo(baggageMap.get("key2"));
  }

  @Test
  void baggage_replacement() {
    SpanShim spanShim = new SpanShim(span);
    SpanContextShim contextShim1 = (SpanContextShim) spanShim.context();

    spanShim.setBaggageItem("key1", "value1");
    SpanContextShim contextShim2 = (SpanContextShim) spanShim.context();
    assertThat(contextShim2).isNotEqualTo(contextShim1);
    assertThat(contextShim1.baggageItems().iterator().hasNext()).isFalse(); /* original, empty */
    assertThat(contextShim2.baggageItems().iterator()).hasNext(); /* updated, with values */
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Test
  void baggage_multipleThreads() throws Exception {
    ExecutorService executor = Executors.newCachedThreadPool();
    SpanShim spanShim = new SpanShim(span);
    int baggageItemsCount = 100;

    IntStream.range(0, baggageItemsCount)
        .forEach(i -> executor.execute(() -> spanShim.setBaggageItem("key-" + i, "value-" + i)));
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    for (int i = 0; i < baggageItemsCount; i++) {
      assertThat(spanShim.getBaggageItem("key-" + i)).isEqualTo("value-" + i);
    }
  }

  @Test
  void finish_micros() {
    SpanShim spanShim = new SpanShim(span);
    long micros = 123447307984L;
    spanShim.finish(micros);
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getEndEpochNanos()).isEqualTo(micros * 1000L);
  }

  @Test
  public void log_error() {
    SpanShim spanShim = new SpanShim(span);
    Map<String, Object> fields = createErrorFields();
    spanShim.log(fields);
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    verifyErrorEvent(spanData);
  }

  @Test
  public void log_error_with_timestamp() {
    SpanShim spanShim = new SpanShim(span);
    Map<String, Object> fields = createErrorFields();
    long micros = 123447307984L;
    spanShim.log(micros, fields);
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    verifyErrorEvent(spanData);
  }

  @Test
  public void log_exception() {
    SpanShim spanShim = new SpanShim(span);
    Map<String, Object> fields = createExceptionFields();
    spanShim.log(fields);
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getEvents()).hasSize(1);

    verifyExceptionEvent(spanData);
  }

  @Test
  public void log_error_with_exception() {
    SpanShim spanShim = new SpanShim(span);
    Map<String, Object> fields = createExceptionFields();
    fields.putAll(createErrorFields());

    long micros = 123447307984L;
    spanShim.log(micros, fields);
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    verifyErrorEvent(spanData);
  }

  @Test
  public void log_exception_with_timestamp() {
    SpanShim spanShim = new SpanShim(span);
    Map<String, Object> fields = createExceptionFields();
    long micros = 123447307984L;
    spanShim.log(micros, fields);
    SpanData spanData = ((ReadableSpan) span).toSpanData();

    verifyExceptionEvent(spanData);
    assertThat(spanData.getEvents().get(0).getEpochNanos()).isEqualTo(micros * 1000L);
  }

  @Test
  public void log_fields() {
    SpanShim spanShim = new SpanShim(span);
    spanShim.log(putKeyValuePairsToMap(new HashMap<>()));
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    verifyAttributes(spanData.getEvents().get(0));
  }

  @Test
  void log_micros() {
    SpanShim spanShim = new SpanShim(span);
    long micros = 123447307984L;
    spanShim.log(micros, "event");
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getEvents().get(0).getEpochNanos()).isEqualTo(micros * 1000L);
  }

  @Test
  void log_fields_micros() {
    SpanShim spanShim = new SpanShim(span);
    long micros = 123447307984L;
    spanShim.log(micros, putKeyValuePairsToMap(new HashMap<>()));
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    EventData eventData = spanData.getEvents().get(0);
    assertThat(eventData.getEpochNanos()).isEqualTo(micros * 1000L);
    verifyAttributes(eventData);
  }

  private static Map<String, Object> createErrorFields() {
    Map<String, Object> fields = new HashMap<>();
    fields.put(Fields.EVENT, "error");
    fields.put(Fields.ERROR_OBJECT, new RuntimeException());

    putKeyValuePairsToMap(fields);
    return fields;
  }

  private static void verifyErrorEvent(SpanData spanData) {
    assertThat(spanData.getEvents()).hasSize(1);

    EventData eventData = spanData.getEvents().get(0);
    assertThat(eventData.getName()).isEqualTo("exception");

    verifyAttributes(eventData);
  }

  private static Map<String, Object> createExceptionFields() {
    Map<String, Object> fields = new HashMap<>();
    fields.put(Fields.EVENT, "error");
    fields.put(Fields.ERROR_KIND, "kind");
    fields.put(Fields.MESSAGE, "message");
    fields.put(Fields.STACK, "stack");

    putKeyValuePairsToMap(fields);
    return fields;
  }

  private static Map<String, Object> putKeyValuePairsToMap(Map<String, Object> fields) {
    fields.put("keyForString", "value");
    fields.put("keyForInt", 1);
    fields.put("keyForDouble", 1.0);
    fields.put("keyForBoolean", true);
    return fields;
  }

  private static void verifyExceptionEvent(SpanData spanData) {
    assertThat(spanData.getEvents()).hasSize(1);

    EventData eventData = spanData.getEvents().get(0);
    assertThat(eventData.getName()).isEqualTo("exception");
    assertThat(
            eventData
                .getAttributes()
                .get(AttributeKey.stringKey(SemanticAttributes.EXCEPTION_TYPE.getKey())))
        .isEqualTo("kind");
    assertThat(
            eventData
                .getAttributes()
                .get(AttributeKey.stringKey(SemanticAttributes.EXCEPTION_MESSAGE.getKey())))
        .isEqualTo("message");
    assertThat(
            eventData
                .getAttributes()
                .get(AttributeKey.stringKey(SemanticAttributes.EXCEPTION_STACKTRACE.getKey())))
        .isEqualTo("stack");

    verifyAttributes(eventData);
  }

  private static void verifyAttributes(EventData eventData) {
    assertThat(eventData.getAttributes().get(AttributeKey.stringKey("keyForString")))
        .isEqualTo("value");
    assertThat(eventData.getAttributes().get(AttributeKey.longKey("keyForInt"))).isEqualTo(1);
    assertThat(eventData.getAttributes().get(AttributeKey.doubleKey("keyForDouble")))
        .isEqualTo(1.0);
    assertThat(eventData.getAttributes().get(AttributeKey.booleanKey("keyForBoolean"))).isTrue();
  }
}
