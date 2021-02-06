/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.opentracingshim.TestUtils.getBaggageMap;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpanShimTest {

  private final SdkTracerProvider tracerSdkFactory = SdkTracerProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("SpanShimTest");
  private final TelemetryInfo telemetryInfo = new TelemetryInfo(tracer, ContextPropagators.noop());
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
    assertThat(span.getSpanContext().getTraceIdHex().toString()).isEqualTo(contextShim.toTraceId());
    assertThat(span.getSpanContext().getSpanIdHex().toString()).isEqualTo(contextShim.toSpanId());
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
}
