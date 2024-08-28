/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.events.EventLogger;
import io.opentelemetry.api.incubator.events.EventLoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;

class EventToSpanEventBridgeTest {

  @Test
  void demo() {
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    TracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build();

    EventLoggerProvider eventLoggerProvider =
        SdkEventLoggerProvider.create(
            SdkLoggerProvider.builder()
                .addLogRecordProcessor(EventToSpanEventBridge.create())
                .build());

    Tracer tracer = tracerProvider.get("tracer");
    EventLogger eventLogger = eventLoggerProvider.get("event-logger");

    // Emit an event when a span is being recorded. This should be bridged to a span event.
    Span span = tracer.spanBuilder("span").startSpan();
    try (Scope unused = span.makeCurrent()) {
      eventLogger
          .builder("my.event-name")
          .setSeverity(Severity.DEBUG)
          .put("foo", "bar")
          .put("number", 1)
          .setAttributes(Attributes.builder().put("color", "red").build())
          .emit();
    } finally {
      span.end();
    }

    // Emit an event when a span is not being recorded. This should be dropped.
    eventLogger
        .builder("my.event-name")
        .setSeverity(Severity.DEBUG)
        .put("foo", "baz")
        .put("number", 2)
        .setAttributes(Attributes.builder().put("color", "red").build())
        .emit();

    // Assert that first emitted event was bridged to the span
    OpenTelemetryAssertions.assertThat(spanExporter.getFinishedSpanItems())
        .satisfiesExactly(
            spanData ->
                OpenTelemetryAssertions.assertThat(spanData)
                    .hasName("span")
                    .hasEventsSatisfyingExactly(
                        spanEvent ->
                            spanEvent
                                .hasName("my.event-name")
                                .hasAttributes(
                                    Attributes.builder()
                                        // event body should be bridged to span event field with
                                        // type AnyValue, but that doesn't exist (yet) so we string
                                        // encode it.
                                        .put("body", "[number=1, foo=bar]")
                                        .put("severity", "DEBUG")
                                        .put("color", "red")
                                        .build())));
  }
}
