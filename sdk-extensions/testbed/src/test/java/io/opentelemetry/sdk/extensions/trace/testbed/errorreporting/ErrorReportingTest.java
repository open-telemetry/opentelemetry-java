/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.testbed.errorreporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.sdk.extensions.trace.testbed.TestUtils;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

@SuppressWarnings("FutureReturnValueIgnored")
public final class ErrorReportingTest {

  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerSdkManagement(sdk).build();
  private final Tracer tracer = sdk.get(ErrorReportingTest.class.getName());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  /* Very simple error handling **/
  @Test
  void testSimpleError() {
    Span span = tracer.spanBuilder("one").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      throw new RuntimeException("Invalid state");
    } catch (Exception e) {
      span.setStatus(StatusCode.ERROR);
    } finally {
      span.end();
    }

    assertThat(Span.current()).isSameAs(Span.getInvalid());

    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getStatus().getCanonicalCode()).isEqualTo(StatusCode.ERROR);
  }

  /* Error handling in a callback capturing/activating the Span */
  @Test
  void testCallbackError() {
    final Span span = tracer.spanBuilder("one").startSpan();
    executor.submit(
        () -> {
          try (Scope ignored = span.makeCurrent()) {
            throw new RuntimeException("Invalid state");
          } catch (Exception exc) {
            span.setStatus(StatusCode.ERROR);
          } finally {
            span.end();
          }
        });

    await()
        .atMost(5, TimeUnit.SECONDS)
        .until(TestUtils.finishedSpansSize(inMemoryTracing.getSpanExporter()), equalTo(1));

    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getStatus().getCanonicalCode()).isEqualTo(StatusCode.ERROR);
  }

  /* Error handling for a max-retries task (such as url fetching).
   * We log the error at each retry. */
  @Test
  void testErrorRecovery() {
    final int maxRetries = 1;
    int retries = 0;
    Span span = tracer.spanBuilder("one").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      while (retries++ < maxRetries) {
        try {
          throw new RuntimeException("No url could be fetched");
        } catch (final Exception exc) {
          span.addEvent("error");
        }
      }
    }

    span.setStatus(StatusCode.ERROR); // Could not fetch anything.
    span.end();

    assertThat(Span.current()).isSameAs(Span.getInvalid());

    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getStatus().getCanonicalCode()).isEqualTo(StatusCode.ERROR);

    List<Event> events = spans.get(0).getEvents();
    assertEquals(events.size(), maxRetries);
    assertEquals(events.get(0).getName(), "error");
  }

  /* Error handling for a mocked layer automatically capturing/activating
   * the Span for a submitted Runnable. */
  @Test
  void testInstrumentationLayer() {
    Span span = tracer.spanBuilder("one").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      // ScopedRunnable captures the active Span at this time.
      executor.submit(
          new ScopedRunnable(
              () -> {
                try {
                  throw new RuntimeException("Invalid state");
                } catch (Exception exc) {
                  Span.current().setStatus(StatusCode.ERROR);
                } finally {
                  Span.current().end();
                }
              },
              tracer));
    }

    await()
        .atMost(5, TimeUnit.SECONDS)
        .until(TestUtils.finishedSpansSize(inMemoryTracing.getSpanExporter()), equalTo(1));

    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(spans.size(), 1);
    assertEquals(spans.get(0).getStatus().getCanonicalCode(), StatusCode.ERROR);
  }

  private static class ScopedRunnable implements Runnable {
    Runnable runnable;
    Tracer tracer;
    Span span;

    private ScopedRunnable(Runnable runnable, Tracer tracer) {
      this.runnable = runnable;
      this.tracer = tracer;
      this.span = Span.current();
    }

    @Override
    public void run() {
      // No error reporting is done, as we are a simple wrapper.
      try (Scope ignored = span.makeCurrent()) {
        runnable.run();
      }
    }
  }
}
