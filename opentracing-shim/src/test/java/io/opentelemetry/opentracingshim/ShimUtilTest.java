/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShimUtilTest {

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
  void spanWrapper() {
    SpanShim shim = new SpanShim(telemetryInfo, span);
    assertThat(ShimUtil.getSpanShim(shim)).isEqualTo(shim);
    assertThat(ShimUtil.getSpanShim(new SpanWrapper(shim))).isEqualTo(shim);
    assertThatThrownBy(() -> ShimUtil.getSpanShim(new SpanWrapper("not a span")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("span wrapper didn't return a span: java.lang.String");
    assertThatThrownBy(() -> ShimUtil.getSpanShim(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("span is not a valid SpanShim object: null");
  }

  @Test
  void getContextShim() {
    SpanContextShim contextShim = new SpanContextShim(new SpanShim(telemetryInfo, span));
    assertThat(ShimUtil.getContextShim(contextShim)).isEqualTo(contextShim);
    assertThatThrownBy(() -> ShimUtil.getContextShim(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("context is not a valid SpanContextShim object: null");
  }
}