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
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import io.opentracing.log.Fields;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpanShimTest {

  private final SdkTracerProvider tracerSdkFactory = SdkTracerProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("SpanShimTest");
  private final TelemetryInfo telemetryInfo =
      new TelemetryInfo(tracer, OpenTracingPropagators.builder().build());
  private Span span;

  private static final String SPAN_NAME = "Span";

  @BeforeEach
  void setUp() {
    span = telemetryInfo.tracer().spanBuilder(SPAN_NAME).startSpan();
  }

  @AfterEach
  void tearDown() {
    span.end();
  }

  @Test
  void context_simple() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);

    SpanContextShim contextShim = (SpanContextShim) spanShim.context();
    assertThat(contextShim).isNotNull();
    assertThat(span.getSpanContext()).isEqualTo(contextShim.getSpanContext());
    assertThat(span.getSpanContext().getTraceId().toString()).isEqualTo(contextShim.toTraceId());
    assertThat(span.getSpanContext().getSpanId().toString()).isEqualTo(contextShim.toSpanId());
    assertThat(contextShim.baggageItems().iterator().hasNext()).isFalse();
  }

  @Test
  void baggage() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);

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
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    SpanContextShim contextShim1 = (SpanContextShim) spanShim.context();

    spanShim.setBaggageItem("key1", "value1");
    SpanContextShim contextShim2 = (SpanContextShim) spanShim.context();
    assertThat(contextShim2).isNotEqualTo(contextShim1);
    assertThat(contextShim1.baggageItems().iterator().hasNext()).isFalse(); /* original, empty */
    assertThat(contextShim2.baggageItems().iterator()).hasNext(); /* updated, with values */
  }

  @Test
  void baggage_differentShimObjs() {
    SpanShim spanShim1 = new SpanShim(telemetryInfo, span);
    spanShim1.setBaggageItem("key1", "value1");

    /* Baggage should be synchronized among different SpanShim objects
     * referring to the same Span.*/
    SpanShim spanShim2 = new SpanShim(telemetryInfo, span);
    spanShim2.setBaggageItem("key1", "value2");
    assertThat(spanShim1.getBaggageItem("key1")).isEqualTo("value2");
    assertThat(spanShim2.getBaggageItem("key1")).isEqualTo("value2");
    assertThat(getBaggageMap(spanShim2.context().baggageItems()))
        .isEqualTo(getBaggageMap(spanShim1.context().baggageItems()));
  }

  @Test
  void finish_micros() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    long micros = 123447307984L;
    spanShim.finish(micros);
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getEndEpochNanos()).isEqualTo(micros * 1000L);
  }

  @Test
  public void log_error() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    Map<String, Object> fields = createErrorFields();
    spanShim.log(fields);
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    verifyErrorEvent(spanData);
  }

  @Test
  public void log_error_with_timestamp() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    Map<String, Object> fields = createErrorFields();
    long micros = 123447307984L;
    spanShim.log(micros, fields);
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    verifyErrorEvent(spanData);
  }

  @Test
  public void log_exception() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    Map<String, Object> fields = createExceptionFields();
    spanShim.log(fields);
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getEvents()).hasSize(1);

    verifyExceptionEvent(spanData);
  }

  @Test
  public void log_error_with_exception() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    final Map<String, Object> fields = createExceptionFields();
    fields.putAll(createErrorFields());

    long micros = 123447307984L;
    spanShim.log(micros, fields);
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    verifyErrorEvent(spanData);
  }

  @Test
  public void log_exception_with_timestamp() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    Map<String, Object> fields = createExceptionFields();
    long micros = 123447307984L;
    spanShim.log(micros, fields);
    SpanData spanData = ((ReadableSpan) span).toSpanData();

    verifyExceptionEvent(spanData);
    assertThat(spanData.getEvents().get(0).getEpochNanos()).isEqualTo(micros * 1000L);
  }

  @Test
  public void log_fields() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    spanShim.log(putKeyValuePairsToMap(new HashMap<>()));
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    verifyAttributes(spanData.getEvents().get(0));
  }

  @Test
  void log_micros() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
    long micros = 123447307984L;
    spanShim.log(micros, "event");
    SpanData spanData = ((ReadableSpan) span).toSpanData();
    assertThat(spanData.getEvents().get(0).getEpochNanos()).isEqualTo(micros * 1000L);
  }

  @Test
  void log_fields_micros() {
    SpanShim spanShim = new SpanShim(telemetryInfo, span);
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
