/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.errorreporting;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("FutureReturnValueIgnored")
public final class ErrorReportingTest {
  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final Tracer tracer = OpenTracingShim.createTracerShim(otelTesting.getOpenTelemetry());
  private final ExecutorService executor = Executors.newCachedThreadPool();

  /* Very simple error handling **/
  @Test
  void testSimpleError() {
    Span span = tracer.buildSpan("one").start();
    try (Scope scope = tracer.activateSpan(span)) {
      throw new RuntimeException("Invalid state");
    } catch (Exception e) {
      Tags.ERROR.set(span, true);
    } finally {
      span.finish();
    }

    assertThat(tracer.scopeManager().activeSpan()).isNull();

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(1);
    assertThat(StatusCode.ERROR).isEqualTo(spans.get(0).getStatus().getCanonicalCode());
  }

  /* Error handling in a callback capturing/activating the Span */
  @Test
  void testCallbackError() {
    final Span span = tracer.buildSpan("one").start();
    executor.submit(
        () -> {
          try (Scope scope = tracer.activateSpan(span)) {
            throw new RuntimeException("Invalid state");
          } catch (Exception exc) {
            Tags.ERROR.set(span, true);
          } finally {
            span.finish();
          }
        });

    await().atMost(5, TimeUnit.SECONDS).until(finishedSpansSize(otelTesting), equalTo(1));

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(1);
    assertThat(StatusCode.ERROR).isEqualTo(spans.get(0).getStatus().getCanonicalCode());
  }

  /* Error handling for a max-retries task (such as url fetching).
   * We log the Exception at each retry. */
  @Test
  void testErrorRecovery() {
    final int maxRetries = 1;
    int retries = 0;

    Span span = tracer.buildSpan("one").start();
    try (Scope scope = tracer.activateSpan(span)) {
      while (retries++ < maxRetries) {
        try {
          throw new RuntimeException("No url could be fetched");
        } catch (final Exception exc) {
          Map<String, Object> errorMap = new HashMap<>();
          errorMap.put(Fields.EVENT, Tags.ERROR.getKey());
          errorMap.put(Fields.ERROR_OBJECT, exc);
          span.log(errorMap);
        }
      }
    }

    Tags.ERROR.set(span, true); // Could not fetch anything.
    span.finish();

    assertThat(tracer.scopeManager().activeSpan()).isNull();

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(1);
    assertThat(StatusCode.ERROR).isEqualTo(spans.get(0).getStatus().getCanonicalCode());

    List<Event> events = spans.get(0).getEvents();
    assertThat(events).hasSize(maxRetries);
    assertThat(Tags.ERROR.getKey()).isEqualTo(events.get(0).getName());
    /* TODO: Handle actual objects being passed to log/events. */
    /*assertNotNull(events.get(0).getEvent().getAttributes().get(Fields.ERROR_OBJECT));*/
  }

  /* Error handling for a mocked layer automatically capturing/activating
   * the Span for a submitted Runnable. */
  @Test
  void testInstrumentationLayer() {
    Span span = tracer.buildSpan("one").start();
    try (Scope scope = tracer.activateSpan(span)) {

      // ScopedRunnable captures the active Span at this time.
      executor.submit(
          new ScopedRunnable(
              () -> {
                try {
                  throw new RuntimeException("Invalid state");
                } catch (Exception exc) {
                  Tags.ERROR.set(tracer.activeSpan(), true);
                } finally {
                  tracer.activeSpan().finish();
                }
              },
              tracer));
    }

    await().atMost(5, TimeUnit.SECONDS).until(finishedSpansSize(otelTesting), equalTo(1));

    List<SpanData> spans = otelTesting.getSpans();
    assertThat(spans).hasSize(1);
    assertThat(StatusCode.ERROR).isEqualTo(spans.get(0).getStatus().getCanonicalCode());
  }

  static class ScopedRunnable implements Runnable {
    Runnable runnable;
    Tracer tracer;
    Span span;

    public ScopedRunnable(Runnable runnable, Tracer tracer) {
      this.runnable = runnable;
      this.tracer = tracer;
      this.span = tracer.activeSpan();
    }

    @Override
    public void run() {
      // No error reporting is done, as we are a simple wrapper.
      try (Scope scope = tracer.activateSpan(span)) {
        runnable.run();
      }
    }
  }
}
