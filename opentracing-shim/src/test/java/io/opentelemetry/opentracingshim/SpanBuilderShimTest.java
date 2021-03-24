/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static io.opentelemetry.opentracingshim.TestUtils.getBaggageMap;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpanBuilderShimTest {

  private final SdkTracerProvider tracerSdkFactory = SdkTracerProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("SpanShimTest");
  private final TelemetryInfo telemetryInfo =
      new TelemetryInfo(tracer, Propagators.builder().build());

  private static final String SPAN_NAME = "Span";

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void baggage_parent() {
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).start();
    try {
      parentSpan.setBaggageItem("key1", "value1");

      SpanShim childSpan =
          (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).asChildOf(parentSpan).start();
      try {
        assertThat("value1").isEqualTo(childSpan.getBaggageItem("key1"));
        assertThat(getBaggageMap(parentSpan.context().baggageItems()))
            .isEqualTo(getBaggageMap(childSpan.context().baggageItems()));
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void baggage_parentContext() {
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).start();
    try {
      parentSpan.setBaggageItem("key1", "value1");

      SpanShim childSpan =
          (SpanShim)
              new SpanBuilderShim(telemetryInfo, SPAN_NAME).asChildOf(parentSpan.context()).start();
      try {
        assertThat("value1").isEqualTo(childSpan.getBaggageItem("key1"));
        assertThat(getBaggageMap(parentSpan.context().baggageItems()))
            .isEqualTo(getBaggageMap(childSpan.context().baggageItems()));
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void parent_NullContextShim() {
    /* SpanContextShim is null until Span.context() or Span.getBaggageItem() are called.
     * Verify a null SpanContextShim in the parent is handled properly. */
    SpanShim parentSpan = (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).start();
    try {
      SpanShim childSpan =
          (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).asChildOf(parentSpan).start();
      try {
        assertThat(childSpan.context().baggageItems().iterator().hasNext()).isFalse();
      } finally {
        childSpan.finish();
      }
    } finally {
      parentSpan.finish();
    }
  }

  @Test
  void withStartTimestamp() {
    long micros = 123447307984L;
    SpanShim spanShim =
        (SpanShim) new SpanBuilderShim(telemetryInfo, SPAN_NAME).withStartTimestamp(micros).start();
    SpanData spanData = ((ReadableSpan) spanShim.getSpan()).toSpanData();
    assertThat(spanData.getStartEpochNanos()).isEqualTo(micros * 1000L);
  }
}
