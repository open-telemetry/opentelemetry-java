/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit5;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OpenTelemetryExtensionTest {

  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private static OpenTelemetry openTelemetryBeforeTest;

  // Class callbacks happen outside the rule so we can verify the restoration behavior in them.
  @BeforeAll
  public static void beforeTest() {
    openTelemetryBeforeTest = OpenTelemetry.get();
  }

  @AfterAll
  public static void afterTest() {
    assertThat(OpenTelemetry.get()).isSameAs(openTelemetryBeforeTest);
  }

  private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer("test");

  @Test
  public void exportSpan() {
    tracer.spanBuilder("test").startSpan().end();

    assertThat(otelTesting.getSpans())
        .hasOnlyOneElementSatisfying(span -> assertThat(span.getName()).isEqualTo("test"));
    // Spans cleared between tests, not when retrieving
    assertThat(otelTesting.getSpans())
        .hasOnlyOneElementSatisfying(span -> assertThat(span.getName()).isEqualTo("test"));
  }

  // We have two tests to verify spans get cleared up between tests.
  @Test
  public void exportSpanAgain() {
    tracer.spanBuilder("test").startSpan().end();

    assertThat(otelTesting.getSpans())
        .hasOnlyOneElementSatisfying(span -> assertThat(span.getName()).isEqualTo("test"));
    // Spans cleared between tests, not when retrieving
    assertThat(otelTesting.getSpans())
        .hasOnlyOneElementSatisfying(span -> assertThat(span.getName()).isEqualTo("test"));
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

    otelTesting
        .assertTraces()
        .hasTracesSatisfyingExactly(
            trace ->
                trace.hasSpansSatisfyingExactly(s -> s.hasName("testa1"), s -> s.hasName("testa2")),
            trace ->
                trace.hasSpansSatisfyingExactly(
                    s -> s.hasName("testb1"), s -> s.hasName("testb2")));
  }
}
