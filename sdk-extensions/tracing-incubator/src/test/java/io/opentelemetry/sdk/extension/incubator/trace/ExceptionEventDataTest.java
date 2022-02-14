/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.extension.incubator.trace.data.ExceptionEventData;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExceptionEventDataTest {

  @Mock private SpanProcessor spanProcessor;
  @Captor private ArgumentCaptor<ReadableSpan> spanArgumentCaptor;

  private TestClock testClock;
  private OpenTelemetry openTelemetry;

  @BeforeEach
  void setUp() {

    testClock = TestClock.create();
    openTelemetry =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(spanProcessor)
                    .setClock(testClock)
                    .build())
            .build();
  }

  @Test
  void recordException() {
    Exception exception = new IllegalStateException("there was an exception");
    String stackTrace = stackTrace(exception);

    testClock.advance(Duration.ofNanos(1000));
    long timestamp = testClock.now();

    Span span = createSpan();
    span.recordException(exception);
    span.end();

    verify(spanProcessor).onEnd(spanArgumentCaptor.capture());
    ReadableSpan capturedSpan = spanArgumentCaptor.getValue();
    SpanData spanData = capturedSpan.toSpanData();
    List<EventData> events = spanData.getEvents();
    assertThat(events).hasSize(1);

    EventData event = events.get(0);
    assertThat(event.getName()).isEqualTo(SemanticAttributes.EXCEPTION_EVENT_NAME);
    assertThat(event.getEpochNanos()).isEqualTo(timestamp);
    assertThat(event.getAttributes())
        .isEqualTo(
            Attributes.of(
                SemanticAttributes.EXCEPTION_TYPE, "java.lang.IllegalStateException",
                SemanticAttributes.EXCEPTION_MESSAGE, "there was an exception",
                SemanticAttributes.EXCEPTION_STACKTRACE, stackTrace));
    assertThat(event.getTotalAttributeCount()).isEqualTo(3);

    assertThat(event).isInstanceOf(ExceptionEventData.class);
    ExceptionEventData exceptionEvent = (ExceptionEventData) event;
    assertThat(exceptionEvent.getException()).isSameAs(exception);
    assertThat(exceptionEvent.getAdditionalAttributes()).isEqualTo(Attributes.empty());
  }

  @Test
  void recordException_noMessage() {
    Exception exception = new IllegalStateException();
    String stackTrace = stackTrace(exception);

    testClock.advance(Duration.ofNanos(1000));
    long timestamp = testClock.now();

    Span span = createSpan();
    span.recordException(exception);
    span.end();

    verify(spanProcessor).onEnd(spanArgumentCaptor.capture());
    ReadableSpan capturedSpan = spanArgumentCaptor.getValue();
    SpanData spanData = capturedSpan.toSpanData();
    List<EventData> events = spanData.getEvents();
    assertThat(events).hasSize(1);

    EventData event = events.get(0);
    assertThat(event.getName()).isEqualTo(SemanticAttributes.EXCEPTION_EVENT_NAME);
    assertThat(event.getEpochNanos()).isEqualTo(timestamp);
    assertThat(event.getAttributes())
        .isEqualTo(
            Attributes.of(
                SemanticAttributes.EXCEPTION_TYPE,
                "java.lang.IllegalStateException",
                SemanticAttributes.EXCEPTION_STACKTRACE,
                stackTrace));
    assertThat(event.getTotalAttributeCount()).isEqualTo(2);

    assertThat(event).isInstanceOf(ExceptionEventData.class);
    ExceptionEventData exceptionEvent = (ExceptionEventData) event;
    assertThat(exceptionEvent.getException()).isSameAs(exception);
    assertThat(exceptionEvent.getAdditionalAttributes()).isEqualTo(Attributes.empty());
  }

  @Test
  void recordException_innerClassException() {
    Exception exception = new InnerClassException("there was an exception");
    String stackTrace = stackTrace(exception);

    testClock.advance(Duration.ofNanos(1000));
    long timestamp = testClock.now();

    Span span = createSpan();
    span.recordException(exception);
    span.end();

    verify(spanProcessor).onEnd(spanArgumentCaptor.capture());
    ReadableSpan capturedSpan = spanArgumentCaptor.getValue();
    SpanData spanData = capturedSpan.toSpanData();
    List<EventData> events = spanData.getEvents();
    assertThat(events).hasSize(1);

    EventData event = events.get(0);
    assertThat(event.getName()).isEqualTo(SemanticAttributes.EXCEPTION_EVENT_NAME);
    assertThat(event.getEpochNanos()).isEqualTo(timestamp);
    assertThat(event.getAttributes())
        .isEqualTo(
            Attributes.of(
                SemanticAttributes.EXCEPTION_TYPE,
                    "io.opentelemetry.sdk.extension.incubator.trace.ExceptionEventDataTest.InnerClassException",
                SemanticAttributes.EXCEPTION_MESSAGE, "there was an exception",
                SemanticAttributes.EXCEPTION_STACKTRACE, stackTrace));
    assertThat(event.getTotalAttributeCount()).isEqualTo(3);

    assertThat(event).isInstanceOf(ExceptionEventData.class);
    ExceptionEventData exceptionEvent = (ExceptionEventData) event;
    assertThat(exceptionEvent.getException()).isSameAs(exception);
    assertThat(exceptionEvent.getAdditionalAttributes()).isEqualTo(Attributes.empty());
  }

  @Test
  void recordException_additionalAttributes() {
    Exception exception = new IllegalStateException("there was an exception");
    String stackTrace = stackTrace(exception);

    testClock.advance(Duration.ofNanos(1000));
    long timestamp = testClock.now();

    Span span = createSpan();
    span.recordException(
        exception,
        Attributes.of(
            stringKey("key1"),
            "this is an additional attribute",
            stringKey("exception.message"),
            "this is a precedence attribute"));
    span.end();

    verify(spanProcessor).onEnd(spanArgumentCaptor.capture());
    ReadableSpan capturedSpan = spanArgumentCaptor.getValue();
    SpanData spanData = capturedSpan.toSpanData();
    List<EventData> events = spanData.getEvents();
    assertThat(events).hasSize(1);

    EventData event = events.get(0);
    assertThat(event.getName()).isEqualTo(SemanticAttributes.EXCEPTION_EVENT_NAME);
    assertThat(event.getEpochNanos()).isEqualTo(timestamp);
    assertThat(event.getAttributes())
        .isEqualTo(
            Attributes.builder()
                .put(SemanticAttributes.EXCEPTION_TYPE, "java.lang.IllegalStateException")
                .put(SemanticAttributes.EXCEPTION_MESSAGE, "this is a precedence attribute")
                .put(SemanticAttributes.EXCEPTION_STACKTRACE, stackTrace)
                .put("key1", "this is an additional attribute")
                .build());
    assertThat(event.getTotalAttributeCount()).isEqualTo(4);

    assertThat(event).isInstanceOf(ExceptionEventData.class);
    ExceptionEventData exceptionEvent = (ExceptionEventData) event;
    assertThat(exceptionEvent.getException()).isSameAs(exception);
    assertThat(exceptionEvent.getAdditionalAttributes())
        .isEqualTo(
            Attributes.of(
                stringKey("key1"),
                "this is an additional attribute",
                stringKey("exception.message"),
                "this is a precedence attribute"));
  }

  private Span createSpan() {
    return openTelemetry.getTracer("test").spanBuilder("test").startSpan();
  }

  private static String stackTrace(Throwable exception) {
    StringWriter stringWriter = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
      exception.printStackTrace(printWriter);
    }
    return stringWriter.toString();
  }

  private static class InnerClassException extends Exception {
    public InnerClassException(String message) {
      super(message);
    }
  }
}
