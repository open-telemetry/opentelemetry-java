/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit4;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class OpenTelemetryRuleTest {

  @Rule public OpenTelemetryRule otelTesting = OpenTelemetryRule.create();

  private Tracer tracer;
  private Meter meter;

  @Before
  public void setup() {
    tracer = otelTesting.getOpenTelemetry().getTracer("test");
    meter = otelTesting.getOpenTelemetry().getMeter("test");
  }

  @Test
  public void getSpans() {
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
  public void getSpansAgain() {
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
  public void getMetrics() {
    LongCounter counter = meter.counterBuilder("counter").build();
    counter.add(1);

    OpenTelemetryAssertions.assertThat(otelTesting.getMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                OpenTelemetryAssertions.assertThat(metricData)
                    .hasName("counter")
                    .hasLongSumSatisfying(
                        sum -> sum.hasPointsSatisfying(point -> point.hasValue(1))));
  }

  // We have two tests to verify metrics get cleared up between tests.
  @Test
  public void getMetricsAgain() {
    LongCounter counter = meter.counterBuilder("counter").build();
    counter.add(1);

    OpenTelemetryAssertions.assertThat(otelTesting.getMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                OpenTelemetryAssertions.assertThat(metricData)
                    .hasName("counter")
                    .hasLongSumSatisfying(
                        sum -> sum.hasPointsSatisfying(point -> point.hasValue(1))));
  }
}
