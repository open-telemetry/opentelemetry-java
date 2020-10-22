/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.extension.jfr;

import io.opentelemetry.OpenTelemetry;
import static org.junit.Assert.assertEquals;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.TracingContextUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.junit.Test;

public class JfrSpanProcessorTest {

  private static final String OPERATION_NAME = "Test Span";
  private final Tracer tracer;

  /** Simple test to validate JFR events for Span and Scope. */
  public JfrSpanProcessorTest() {
      tracer = OpenTelemetry.getGlobalTracer("JfrSpanProcessorTest");
      OpenTelemetrySdk.getTracerManagement().addSpanProcessor(new JfrSpanProcessor());
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
        try (Scope s = TracingContextUtils.currentContextWith(span)) {
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
