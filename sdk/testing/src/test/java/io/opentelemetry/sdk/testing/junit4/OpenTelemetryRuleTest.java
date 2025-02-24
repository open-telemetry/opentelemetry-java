/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit4;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class OpenTelemetryRuleTest {

  @Rule public OpenTelemetryRule otelTesting = OpenTelemetryRule.create();

  private Tracer tracer;
  private Meter meter;
  private Logger logger;

  @Before
  public void setup() {
    tracer = otelTesting.getOpenTelemetry().getTracer("test");
    meter = otelTesting.getOpenTelemetry().getMeter("test");
    logger = otelTesting.getOpenTelemetry().getLogsBridge().get("test");
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

  @Test
  public void getLogRecords() {
    logger.logRecordBuilder().setBody("body").emit();

    assertThat(otelTesting.getLogRecords())
        .singleElement()
        .satisfies(
            logRecordData -> assertThat(logRecordData.getBodyValue()).isEqualTo(Value.of("body")));
    // Logs cleared between tests, not when retrieving
    assertThat(otelTesting.getLogRecords())
        .singleElement()
        .satisfies(
            logRecordData -> assertThat(logRecordData.getBodyValue()).isEqualTo(Value.of("body")));
  }

  // We have two tests to verify logs get cleared up between tests.
  @Test
  public void getLogRecordsAgain() {
    logger.logRecordBuilder().setBody("body").emit();

    assertThat(otelTesting.getLogRecords())
        .singleElement()
        .satisfies(
            logRecordData -> assertThat(logRecordData.getBodyValue()).isEqualTo(Value.of("body")));
    // Logs cleared between tests, not when retrieving
    assertThat(otelTesting.getLogRecords())
        .singleElement()
        .satisfies(
            logRecordData -> assertThat(logRecordData.getBodyValue()).isEqualTo(Value.of("body")));
  }

  @Test
  public void baggageAndTracePropagation() {
    OpenTelemetryRule rule = OpenTelemetryRule.create();
    Span span = rule.getOpenTelemetry().getTracer("test").spanBuilder("test").startSpan();
    try (Scope baggageScope = Baggage.builder().put("key", "value").build().makeCurrent();
        Scope spanScope = span.makeCurrent()) {
      Map<String, String> carrier = new HashMap<>();
      rule.getOpenTelemetry()
          .getPropagators()
          .getTextMapPropagator()
          .inject(Context.current(), carrier, new MapTextMapSetter());
      assertThat(carrier).containsEntry("baggage", "key=value");
      assertThat(carrier).containsKey("traceparent");
    } finally {
      span.end();
    }
  }

  public static class MapTextMapSetter implements TextMapSetter<Map<String, String>> {
    @Override
    public void set(@Nullable Map<String, String> carrier, String key, String value) {
      if (carrier != null) {
        carrier.put(key, value);
      }
    }
  }
}
