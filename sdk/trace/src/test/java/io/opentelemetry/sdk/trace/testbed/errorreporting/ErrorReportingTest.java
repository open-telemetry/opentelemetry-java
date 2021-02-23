/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.testbed.errorreporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.testbed.TestUtils;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("FutureReturnValueIgnored")
public final class ErrorReportingTest {

  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer =
      otelTesting.getOpenTelemetry().getTracer(ErrorReportingTest.class.getName());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  /* Very simple error handling **/
  @Test
  void testSimpleError() {
    Span span = tracer.spanBuilder("one").startSpan();
    try (Scope ignored = span.makeCurrent()) {
      throw new RuntimeException("Invalid state");
    } catch (RuntimeException e) {
      span.setStatus(StatusCode.ERROR);
    } finally {
      span.end();
    }

    assertThat(Span.current()).isSameAs(Span.getInvalid());

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
  }

  /* Error handling in a callback capturing/activating the Span */
  @Test
  void testCallbackError() {
    final Span span = tracer.spanBuilder("one").startSpan();
    executor.submit(
        () -> {
          try (Scope ignored = span.makeCurrent()) {
            throw new RuntimeException("Invalid state");
          } catch (RuntimeException exc) {
            span.setStatus(StatusCode.ERROR);
          } finally {
            span.end();
          }
        });

    await()
        .atMost(Duration.ofSeconds(5))
        .until(TestUtils.finishedSpansSize(otelTesting), equalTo(1));

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
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
        } catch (RuntimeException exc) {
          span.addEvent("error");
        }
      }
    }

    span.setStatus(StatusCode.ERROR); // Could not fetch anything.
    span.end();

    assertThat(Span.current()).isSameAs(Span.getInvalid());

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);

    List<EventData> events = spans.get(0).getEvents();
    assertThat(events).hasSize(maxRetries);
    assertThat("error").isEqualTo(events.get(0).getName());
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
                } catch (RuntimeException exc) {
                  Span.current().setStatus(StatusCode.ERROR);
                } finally {
                  Span.current().end();
                }
              },
              tracer));
    }

    await()
        .atMost(Duration.ofSeconds(5))
        .until(TestUtils.finishedSpansSize(otelTesting), equalTo(1));

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(1);
    assertThat(StatusCode.ERROR).isEqualTo(spans.get(0).getStatus().getStatusCode());
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
