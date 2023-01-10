/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShimUtilTest {

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
  void spanWrapper() {
    SpanShim shim = new SpanShim(span);
    assertThat(ShimUtil.getSpanShim(shim)).isEqualTo(shim);
    assertThat(ShimUtil.getSpanShim(new SpanWrapper(shim))).isEqualTo(shim);
    assertThat(ShimUtil.getSpanShim(new SpanWrapper("not a span"))).isNull();
    assertThat(ShimUtil.getSpanShim(null)).isNull();
  }

  @Test
  void getContextShim() {
    SpanContextShim contextShim = new SpanContextShim(span.getSpanContext(), Baggage.empty());
    assertThat(ShimUtil.getContextShim(contextShim)).isEqualTo(contextShim);
    assertThat(ShimUtil.getContextShim(null)).isNull();
  }
}
