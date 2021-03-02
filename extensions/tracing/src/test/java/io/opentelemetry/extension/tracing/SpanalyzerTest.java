/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class SpanalyzerTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer("test");

  @Test
  void wrapRunnable() {
    new Spanalyzer(tracer)
        .wrap(
            "testSpan",
            () -> {
              Span.current().setAttribute("one", 1);
            })
        .run();

    otelTesting
        .assertTraces()
        .hasTracesSatisfyingExactly(
            traceAssert ->
                traceAssert.hasSpansSatisfyingExactly(
                    spanDataAssert ->
                        spanDataAssert
                            .hasName("testSpan")
                            .hasAttributes(Attributes.of(AttributeKey.longKey("one"), 1L))));
  }

  @Test
  void wrapRunnable_throws() {
    assertThatThrownBy(
            () ->
                new Spanalyzer(tracer)
                    .wrap(
                        "throwingRunnable",
                        (Runnable)
                            () -> {
                              Span.current().setAttribute("one", 1);
                              throw new RuntimeException("failed");
                            })
                    .run())
        .isInstanceOf(RuntimeException.class);

    otelTesting
        .assertTraces()
        .hasTracesSatisfyingExactly(
            traceAssert ->
                traceAssert.hasSpansSatisfyingExactly(
                    span ->
                        span.hasName("throwingRunnable")
                            .hasAttributes(Attributes.of(AttributeKey.longKey("one"), 1L))
                            .hasEventsSatisfying(
                                (events) ->
                                    assertThat(events)
                                        .singleElement()
                                        .satisfies(
                                            eventData ->
                                                assertThat(eventData.getName())
                                                    .isEqualTo("exception")))));
  }

  @Test
  void wrapCallable() throws Exception {
    assertThat(
            new Spanalyzer(tracer)
                .wrap(
                    "spanCallable",
                    () -> {
                      Span.current().setAttribute("one", 1);
                      return "hello";
                    })
                .call())
        .isEqualTo("hello");

    otelTesting
        .assertTraces()
        .hasTracesSatisfyingExactly(
            traceAssert ->
                traceAssert.hasSpansSatisfyingExactly(
                    spanDataAssert ->
                        spanDataAssert
                            .hasName("spanCallable")
                            .hasAttributes(Attributes.of(AttributeKey.longKey("one"), 1L))));
  }

  @Test
  void wrapCallable_throws() throws Exception {
    assertThatThrownBy(
            () ->
                new Spanalyzer(tracer)
                    .wrap(
                        "throwingCallable",
                        () -> {
                          Span.current().setAttribute("one", 1);
                          throw new RuntimeException("failed");
                        })
                    .call())
        .isInstanceOf(RuntimeException.class);

    otelTesting
        .assertTraces()
        .hasTracesSatisfyingExactly(
            traceAssert ->
                traceAssert.hasSpansSatisfyingExactly(
                    spanDataAssert ->
                        spanDataAssert
                            .hasName("throwingCallable")
                            .hasAttributes(Attributes.of(AttributeKey.longKey("one"), 1L))));
  }
}
