/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.errorreporting;

import static io.opentelemetry.opentracingshim.testbed.TestUtils.finishedSpansSize;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.exporters.inmemory.InMemoryTracing;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
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

@SuppressWarnings("FutureReturnValueIgnored")
public final class ErrorReportingTest {
  private final TracerSdkProvider sdk = TracerSdkProvider.builder().build();
  private final OpenTelemetry openTelemetry =
      OpenTelemetry.get().toBuilder().setTracerProvider(sdk).build();
  private final InMemoryTracing inMemoryTracing =
      InMemoryTracing.builder().setTracerSdkManagement(sdk).build();
  private final Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry);
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

    assertNull(tracer.scopeManager().activeSpan());

    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(spans.size(), 1);
    assertEquals(spans.get(0).getStatus().getCanonicalCode(), StatusCode.ERROR);
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

    await()
        .atMost(5, TimeUnit.SECONDS)
        .until(finishedSpansSize(inMemoryTracing.getSpanExporter()), equalTo(1));

    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(spans.size(), 1);
    assertEquals(spans.get(0).getStatus().getCanonicalCode(), StatusCode.ERROR);
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

    assertNull(tracer.scopeManager().activeSpan());

    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(spans.size(), 1);
    assertEquals(spans.get(0).getStatus().getCanonicalCode(), StatusCode.ERROR);

    List<Event> events = spans.get(0).getEvents();
    assertEquals(events.size(), maxRetries);
    assertEquals(events.get(0).getName(), Tags.ERROR.getKey());
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

    await()
        .atMost(5, TimeUnit.SECONDS)
        .until(finishedSpansSize(inMemoryTracing.getSpanExporter()), equalTo(1));

    List<SpanData> spans = inMemoryTracing.getSpanExporter().getFinishedSpanItems();
    assertEquals(spans.size(), 1);
    assertEquals(spans.get(0).getStatus().getCanonicalCode(), StatusCode.ERROR);
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
