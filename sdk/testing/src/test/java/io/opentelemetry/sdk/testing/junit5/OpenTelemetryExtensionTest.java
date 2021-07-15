/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OpenTelemetryExtensionTest {

  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer("test");

  @Test
  public void exportSpan() {
    tracer.spanBuilder("test").startSpan().end();

    assertThat(otelTesting.getSpans())
        .singleElement()
        .satisfies(span -> assertThat(span.getName()).isEqualTo("test"));
    // Spans cleared between tests, not when retrieving
    assertThat(otelTesting.getSpans())
        .singleElement()
        .satisfies(span -> assertThat(span.getName()).isEqualTo("test"));
  }

  // We have two tests to verify spans get cleared up between tests.
  @Test
  public void exportSpanAgain() {
    tracer.spanBuilder("test").startSpan().end();

    assertThat(otelTesting.getSpans())
        .singleElement()
        .satisfies(span -> assertThat(span.getName()).isEqualTo("test"));
    // Spans cleared between tests, not when retrieving
    assertThat(otelTesting.getSpans())
        .singleElement()
        .satisfies(span -> assertThat(span.getName()).isEqualTo("test"));
  }

  @Test
  public void exportTraces() {
    Span span = tracer.spanBuilder("testa1").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      tracer.spanBuilder("testa2").startSpan().end();
    } finally {
      span.end();
    }

    span = tracer.spanBuilder("testb1").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      tracer.spanBuilder("testb2").startSpan().end();
    } finally {
      span.end();
    }

    String traceId =
        otelTesting.getSpans().stream()
            .collect(
                Collectors.groupingBy(
                    SpanData::getTraceId, LinkedHashMap::new, Collectors.toList()))
            .values()
            .stream()
            .findFirst()
            .get()
            .get(0)
            .getTraceId();

    otelTesting
        .assertTraces()
        .hasTracesSatisfyingExactly(
            trace ->
                trace
                    .hasTraceId(traceId)
                    .hasSpansSatisfyingExactly(
                        s -> s.hasName("testa1").hasNoParent(),
                        s ->
                            s.hasName("testa2")
                                .hasParentSpanId(trace.getSpan(0).getSpanId())
                                .hasParent(trace.getSpan(0)))
                    .first()
                    .hasName("testa1"),
            trace ->
                trace
                    .hasSpansSatisfyingExactly(s -> s.hasName("testb1"), s -> s.hasName("testb2"))
                    .filteredOn(s -> s.getName().endsWith("1"))
                    .hasSize(1));

    assertThatThrownBy(
            () ->
                otelTesting
                    .assertTraces()
                    .hasTracesSatisfyingExactly(trace -> trace.hasTraceId("foo")))
        .isInstanceOf(AssertionError.class);

    otelTesting
        .assertTraces()
        .first()
        .hasSpansSatisfyingExactly(s -> s.hasName("testa1"), s -> s.hasName("testa2"));
    otelTesting
        .assertTraces()
        .filteredOn(trace -> trace.size() == 2)
        .hasTracesSatisfyingExactly(
            trace ->
                trace.hasSpansSatisfyingExactly(s -> s.hasName("testa1"), s -> s.hasName("testa2")),
            trace ->
                trace.hasSpansSatisfyingExactly(
                    s -> s.hasName("testb1"), s -> s.hasName("testb2")));
  }
}
