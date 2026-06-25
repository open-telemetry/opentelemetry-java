/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.logs;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class EventToSpanEventBridgeTest {

  private final InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
  private final SdkTracerProvider tracerProvider =
      SdkTracerProvider.builder()
          .setSampler(onlyServerSpans())
          .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
          .build();
  private final TestClock testClock = TestClock.create();
  private final SdkLoggerProvider loggerProvider =
      SdkLoggerProvider.builder()
          .setClock(testClock)
          .addLogRecordProcessor(EventToSpanEventBridge.create())
          .build();
  private final Tracer tracer = tracerProvider.get("tracer");
  private final Logger logger = loggerProvider.get("event-logger");

  private static Sampler onlyServerSpans() {
    return new Sampler() {
      @Override
      public SamplingResult shouldSample(
          Context parentContext,
          String traceId,
          String name,
          SpanKind spanKind,
          Attributes attributes,
          List<LinkData> parentLinks) {
        return SpanKind.SERVER.equals(spanKind)
            ? SamplingResult.recordAndSample()
            : SamplingResult.drop();
      }

      @Override
      public String getDescription() {
        return "description";
      }
    };
  }

  @Test
  void withRecordingSpan_bridgesEventWithAttributes() {
    Span span = tracer.spanBuilder("span").setSpanKind(SpanKind.SERVER).startSpan();
    try (Scope unused = span.makeCurrent()) {
      logger
          .logRecordBuilder()
          .setEventName("my.event-name")
          .setTimestamp(100, TimeUnit.NANOSECONDS)
          .setSeverity(Severity.DEBUG)
          .setAttribute(AttributeKey.stringKey("color"), "red")
          .setAttribute(AttributeKey.stringKey("shape"), "square")
          .emit();
    } finally {
      span.end();
    }

    assertThat(spanExporter.getFinishedSpanItems())
        .satisfiesExactly(
            spanData ->
                assertThat(spanData)
                    .hasName("span")
                    .hasEventsSatisfyingExactly(
                        spanEvent ->
                            spanEvent
                                .hasName("my.event-name")
                                .hasTimestamp(100, TimeUnit.NANOSECONDS)
                                .hasAttributes(
                                    Attributes.of(
                                        stringKey("color"), "red",
                                        stringKey("shape"), "square"))));
  }

  @Test
  void timestamp_fallsBackToObservedTimestamp() {
    // Set observed time to 1ms = 1_000_000 ns via the test clock
    testClock.setTime(Instant.ofEpochMilli(1));

    Span span = tracer.spanBuilder("span").setSpanKind(SpanKind.SERVER).startSpan();
    try (Scope unused = span.makeCurrent()) {
      // No explicit timestamp — SDK sets observedTimestamp from the clock
      logger.logRecordBuilder().setEventName("my.event-name").emit();
    } finally {
      span.end();
    }

    assertThat(spanExporter.getFinishedSpanItems())
        .satisfiesExactly(
            spanData ->
                assertThat(spanData)
                    .hasName("span")
                    .hasEventsSatisfyingExactly(
                        // observedTimestamp is 1ms = 1_000_000 ns
                        spanEvent ->
                            spanEvent
                                .hasName("my.event-name")
                                .hasTimestamp(1_000_000, TimeUnit.NANOSECONDS)));
  }

  @Test
  void nonRecordingSpan_doesNotBridgeEvent() {
    // INTERNAL kind is dropped by the sampler, producing a non-recording span
    Span span = tracer.spanBuilder("span").setSpanKind(SpanKind.INTERNAL).startSpan();
    try (Scope unused = span.makeCurrent()) {
      logger
          .logRecordBuilder()
          .setEventName("my.event-name")
          .setTimestamp(100, TimeUnit.NANOSECONDS)
          .emit();
    } finally {
      span.end();
    }

    assertThat(spanExporter.getFinishedSpanItems())
        .allSatisfy(spanData -> assertThat(spanData.getEvents()).isEmpty());
  }

  @Test
  void differentSpanContext_doesNotBridgeEvent() {
    Span span = tracer.spanBuilder("span").setSpanKind(SpanKind.SERVER).startSpan();
    try (Scope unused = span.makeCurrent()) {
      // Override the log's context to reference a different span
      logger
          .logRecordBuilder()
          .setEventName("my.event-name")
          .setContext(
              Span.wrap(
                      SpanContext.create(
                          IdGenerator.random().generateTraceId(),
                          IdGenerator.random().generateSpanId(),
                          TraceFlags.getDefault(),
                          TraceState.getDefault()))
                  .storeInContext(Context.current()))
          .setTimestamp(100, TimeUnit.NANOSECONDS)
          .emit();
    } finally {
      span.end();
    }

    assertThat(spanExporter.getFinishedSpanItems())
        .allSatisfy(spanData -> assertThat(spanData.getEvents()).isEmpty());
  }

  @Test
  void noCurrentSpan_doesNotBridgeEvent() {
    logger
        .logRecordBuilder()
        .setEventName("my.event-name")
        .setTimestamp(100, TimeUnit.NANOSECONDS)
        .emit();

    assertThat(spanExporter.getFinishedSpanItems()).isEmpty();
  }

  @Test
  void noEventName_doesNotBridgeEvent() {
    Span span = tracer.spanBuilder("span").setSpanKind(SpanKind.SERVER).startSpan();
    try (Scope unused = span.makeCurrent()) {
      // Emit a plain log record (no event name)
      logger.logRecordBuilder().setTimestamp(100, TimeUnit.NANOSECONDS).emit();
    } finally {
      span.end();
    }

    assertThat(spanExporter.getFinishedSpanItems())
        .satisfiesExactly(
            spanData ->
                assertThat(spanData)
                    .hasName("span")
                    .hasEventsSatisfying(events -> assertThat(events).isEmpty()));
  }

  @Test
  void usesContextParameter_notAmbientContext() {
    // span1: NOT made current — its context is passed explicitly to the log record builder
    Span span1 = tracer.spanBuilder("span1").setSpanKind(SpanKind.SERVER).startSpan();
    // span2: the ambient current span during emission
    Span span2 = tracer.spanBuilder("span2").setSpanKind(SpanKind.SERVER).startSpan();
    try (Scope unused = span2.makeCurrent()) {
      logger
          .logRecordBuilder()
          .setEventName("my.event-name")
          .setTimestamp(100, TimeUnit.NANOSECONDS)
          // The context passed to onEmit contains span1, not the ambient span2
          .setContext(span1.storeInContext(Context.root()))
          .emit();
    } finally {
      span1.end();
      span2.end();
    }

    // The bridge MUST use the context parameter (span1), not the ambient context (span2).
    assertThat(spanExporter.getFinishedSpanItems())
        .satisfiesExactlyInAnyOrder(
            spanData ->
                assertThat(spanData)
                    .hasName("span1")
                    .hasEventsSatisfyingExactly(event -> event.hasName("my.event-name")),
            spanData ->
                assertThat(spanData)
                    .hasName("span2")
                    .hasEventsSatisfying(events -> assertThat(events).isEmpty()));
  }
}
