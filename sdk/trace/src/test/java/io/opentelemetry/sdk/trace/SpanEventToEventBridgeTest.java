/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.events.EventLogger;
import io.opentelemetry.api.incubator.events.EventLoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.logs.internal.AnyValueBody;
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;

class SpanEventToEventBridgeTest {

  @Test
  void demo() {
    InMemoryLogRecordExporter logRecordExporter = InMemoryLogRecordExporter.create();
    EventLoggerProvider eventLoggerProvider =
        SdkEventLoggerProvider.create(
            SdkLoggerProvider.builder()
                .addLogRecordProcessor(SimpleLogRecordProcessor.create(logRecordExporter))
                .build());

    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    TracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SpanEventToEventBridge.create(eventLoggerProvider))
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build();

    Tracer tracer = tracerProvider.get("tracer");
    EventLogger eventLogger = eventLoggerProvider.get("event-logger");

    // Emit an event when a span is being recorded. This should be bridged to a span event.
    Span span = tracer.spanBuilder("span").startSpan();
    span.addEvent("event-name", Attributes.builder().put("foo", "bar").put("number", 1).build());
    span.end();

    // Emit an event when a span is not being recorded. This should be dropped.
    eventLogger
        .builder("my.event-name")
        .setSeverity(Severity.DEBUG)
        .put("foo", "baz")
        .put("number", 2)
        .setAttributes(Attributes.builder().put("color", "red").build())
        .emit();

    // Assert the span event was bridged to an event log record
    assertThat(logRecordExporter.getFinishedLogRecordItems())
        .satisfiesExactly(
            logRecordData -> {
              assertThat(logRecordData.getAttributes())
                  .isEqualTo(
                      Attributes.builder().put("event.name", "span-event.event-name").build());
              assertThat(logRecordData.getBody())
                  .isInstanceOf(AnyValueBody.class)
                  .satisfies(
                      // TODO: asert against structured AnyValue
                      body ->
                          assertThat(((AnyValueBody) body).asAnyValue().asString())
                              .isEqualTo("[number=1, foo=bar]"));
            },
            logRecordData -> {
              assertThat(logRecordData.getAttributes())
                  .isEqualTo(
                      Attributes.builder()
                          .put("event.name", "my.event-name")
                          .put("color", "red")
                          .build());
              assertThat(logRecordData.getBody())
                  .isInstanceOf(AnyValueBody.class)
                  .satisfies(
                      // TODO: asert against structured AnyValue
                      body ->
                          assertThat(((AnyValueBody) body).asAnyValue().asString())
                              .isEqualTo("[number=2, foo=baz]"));
            });

    // Assert span is still exported
    assertThat(spanExporter.getFinishedSpanItems())
        .satisfiesExactly(
            spanData ->
                assertThat(spanData)
                    .hasName("span")
                    // TODO: span event is still attached to the span, but shouldn't be.
                    .hasEventsSatisfyingExactly(
                        spanEvent ->
                            spanEvent
                                .hasName("event-name")
                                .hasAttributes(
                                    Attributes.builder()
                                        .put("foo", "bar")
                                        .put("number", 1)
                                        .build())));
  }
}
