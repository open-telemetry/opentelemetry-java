/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.jfr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.junit.jupiter.api.Test;

public class JfrSpanProcessorTest {

  private static final String OPERATION_NAME = "Test Span";
  private final Tracer tracer;

  static {
    ContextStorage.addWrapper(JfrContextStorageWrapper::new);
    OpenTelemetrySdk.getGlobalTracerManagement().addSpanProcessor(new JfrSpanProcessor());
  }

  /** Simple test to validate JFR events for Span and Scope. */
  public JfrSpanProcessorTest() {
    tracer = OpenTelemetry.getGlobalTracer("JfrSpanProcessorTest");
  }

  /**
   * Test basic single span.
   *
   * @throws java.io.IOException on io error
   */
  @Test
  public void basicSpan() throws IOException {
    Path output = Files.createTempFile("test-basic-span", ".jfr");

    try {
      Recording recording = new Recording();
      recording.start();
      Span span;

      try (recording) {

        span = tracer.spanBuilder(OPERATION_NAME).setNoParent().startSpan();
        span.end();

        recording.dump(output);
      }

      List<RecordedEvent> events = RecordingFile.readAllEvents(output);
      assertEquals(1, events.size());
      events.stream()
          .forEach(
              e -> {
                assertEquals(span.getSpanContext().getTraceIdAsHexString(), e.getValue("traceId"));
                assertEquals(span.getSpanContext().getSpanIdAsHexString(), e.getValue("spanId"));
                assertEquals(OPERATION_NAME, e.getValue("operationName"));
              });

    } finally {
      Files.delete(output);
    }
  }

  /**
   * Test basic single span with a scope.
   *
   * @throws java.io.IOException on io error
   * @throws java.lang.InterruptedException interrupted sleep
   */
  @Test
  public void basicSpanWithScope() throws IOException, InterruptedException {
    Path output = Files.createTempFile("test-basic-span-with-scope", ".jfr");

    try {
      Recording recording = new Recording();
      recording.start();
      Span span;

      try (recording) {
        span = tracer.spanBuilder(OPERATION_NAME).setNoParent().startSpan();
        try (Scope s = span.makeCurrent()) {
          Thread.sleep(10);
        }
        span.end();

        recording.dump(output);
      }

      List<RecordedEvent> events = RecordingFile.readAllEvents(output);
      assertEquals(2, events.size());
      events.stream()
          .forEach(
              e -> {
                assertEquals(span.getSpanContext().getTraceIdAsHexString(), e.getValue("traceId"));
                assertEquals(span.getSpanContext().getSpanIdAsHexString(), e.getValue("spanId"));
                if ("Span".equals(e.getEventType().getLabel())) {
                  assertEquals(OPERATION_NAME, e.getValue("operationName"));
                }
              });

    } finally {
      Files.delete(output);
    }
  }
}
