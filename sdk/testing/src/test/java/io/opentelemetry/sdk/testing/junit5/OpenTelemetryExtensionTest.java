/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit5;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
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
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

class OpenTelemetryExtensionTest {

  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer("test");
  private final Meter meter = otelTesting.getOpenTelemetry().getMeter("test");
  private final Logger logger = otelTesting.getOpenTelemetry().getLogsBridge().get("test");

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
  public void assertTraces() {
    Span span = tracer.spanBuilder("testa1").setStartTimestamp(1000, TimeUnit.SECONDS).startSpan();
    try (Scope ignored = span.makeCurrent()) {
      tracer.spanBuilder("testa2").setStartTimestamp(1000, TimeUnit.SECONDS).startSpan().end();
    } finally {
      span.end();
    }

    span = tracer.spanBuilder("testb1").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      tracer.spanBuilder("testb2").startSpan().end();
      tracer
          .spanBuilder("testexception")
          .startSpan()
          .recordException(new IllegalStateException("exception occurred"))
          .end();
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
                    .hasSpansSatisfyingExactly(
                        s -> s.hasName("testb1"),
                        s -> s.hasName("testb2"),
                        s -> s.hasException(new IllegalStateException("exception occurred")))
                    .filteredOn(s -> s.getName().endsWith("1"))
                    .hasSize(1));

    otelTesting
        .assertTraces()
        .hasTracesSatisfyingExactly(
            Arrays.asList(
                trace -> trace.hasTraceId(traceId),
                trace ->
                    trace.hasSpansSatisfyingExactly(
                        Arrays.asList(
                            s -> s.hasName("testb1"),
                            s -> s.hasName("testb2"),
                            s -> s.hasName("testexception")))));

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
                trace.hasSpansSatisfyingExactly(
                    s -> s.hasName("testa1"), s -> s.hasName("testa2")));
  }

  @Test
  void getMetrics() {
    LongCounter counter = meter.counterBuilder("counter").build();
    counter.add(1);

    assertThat(otelTesting.getMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
                    .hasName("counter")
                    .hasLongSumSatisfying(
                        sum -> sum.hasPointsSatisfying(point -> point.hasValue(1))));
  }

  // We have two tests to verify metrics get cleared up between tests.
  @Test
  void getMetricsAgain() {
    LongCounter counter = meter.counterBuilder("counter").build();
    counter.add(1);

    assertThat(otelTesting.getMetrics())
        .satisfiesExactlyInAnyOrder(
            metricData ->
                assertThat(metricData)
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

  // We have two tests to verify spans get cleared up between tests.
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
  void afterAll() {
    // Use a different instance of OpenTelemetryExtension to avoid interfering with other tests
    OpenTelemetryExtension extension = OpenTelemetryExtension.create();

    ExtensionContext parentContext = spy(ExtensionContext.class);
    when(parentContext.getTestClass()).thenReturn(Optional.empty());

    ExtensionContext context = spy(ExtensionContext.class);
    when(context.getParent()).thenReturn(Optional.of(parentContext));

    extension.beforeAll(context);

    Meter meter = extension.getOpenTelemetry().getMeter("meter");
    Tracer tracer = extension.getOpenTelemetry().getTracer("tracer");

    meter.counterBuilder("counter").build().add(10);
    tracer.spanBuilder("span").startSpan().end();
    assertThat(extension.getMetrics()).isNotEmpty();
    assertThat(extension.getSpans()).isNotEmpty();

    extension.afterAll(context);
    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isSameAs(OpenTelemetry.noop());

    meter.counterBuilder("counter").build().add(10);
    tracer.spanBuilder("span").startSpan().end();
    assertThat(extension.getMetrics()).isEmpty();
    assertThat(extension.getSpans()).isEmpty();
  }

  @Test
  void afterAllNestedOnly() {
    // Demonstrate specifically that Nested tests do not reset extension sdk
    OpenTelemetryExtension extension = OpenTelemetryExtension.create();

    ExtensionContext context = spy(ExtensionContext.class);
    when(context.getParent()).thenReturn(Optional.empty());

    extension.beforeAll(context);

    Meter meter = extension.getOpenTelemetry().getMeter("meter");
    Tracer tracer = extension.getOpenTelemetry().getTracer("tracer");

    extension.afterAll(context);

    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isSameAs(OpenTelemetry.noop());

    // GlobalTelemetry was reset, but extension's sdk was not closed
    meter.counterBuilder("counter").build().add(10);
    tracer.spanBuilder("span").startSpan().end();
    assertThat(extension.getMetrics()).isNotEmpty();
    assertThat(extension.getSpans()).isNotEmpty();
  }

  @Test
  void baggageAndTracePropagation() {
    OpenTelemetryExtension extension = OpenTelemetryExtension.create();
    Span span = extension.getOpenTelemetry().getTracer("test").spanBuilder("test").startSpan();
    try (Scope baggageScope = Baggage.builder().put("key", "value").build().makeCurrent();
        Scope spanScope = span.makeCurrent()) {
      Map<String, String> carrier = new HashMap<>();
      extension
          .getOpenTelemetry()
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
