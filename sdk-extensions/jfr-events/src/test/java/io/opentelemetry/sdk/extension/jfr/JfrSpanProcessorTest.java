/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.jfr;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.ContextStorage;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JfrSpanProcessorTest {

  private static final String OPERATION_NAME = "Test Span";

  private SdkTracerProvider sdkTracerProvider;
  private Tracer tracer;

  @BeforeEach
  void setUp() {
    sdkTracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(new JfrSpanProcessor()).build();
    tracer = sdkTracerProvider.get("JfrSpanProcessorTest");
  }

  @AfterEach
  void tearDown() {
    sdkTracerProvider.shutdown();
  }

  static {
    ContextStorage.addWrapper(JfrContextStorageWrapper::new);
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
      assertThat(events).hasSize(1);
      assertThat(events)
          .extracting(e -> e.getValue("traceId"))
          .isEqualTo(span.getSpanContext().getTraceId());
      assertThat(events)
          .extracting(e -> e.getValue("spanId"))
          .isEqualTo(span.getSpanContext().getSpanId());
      assertThat(events).extracting(e -> e.getValue("operationName")).isEqualTo(OPERATION_NAME);
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
      assertThat(events).hasSize(2);
      assertThat(events)
          .extracting(e -> e.getValue("traceId"))
          .isEqualTo(span.getSpanContext().getTraceId());
      assertThat(events)
          .extracting(e -> e.getValue("spanId"))
          .isEqualTo(span.getSpanContext().getSpanId());
      assertThat(events)
          .filteredOn(e -> "Span".equals(e.getEventType().getLabel()))
          .extracting(e -> e.getValue("operationName"))
          .isEqualTo(OPERATION_NAME);

    } finally {
      Files.delete(output);
    }
  }
}
