/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of extended Trace API. */
class ExtendedTraceApiUsageTest {

  /** Demonstrates {@link ExtendedSpanBuilder#setParentFrom(ContextPropagators, Map)}. */
  @Test
  void setParentFrom() {
    // Setup SdkTracerProvider
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            // Default resource used for demonstration purposes
            .setResource(Resource.getDefault())
            // SimpleSpanProcessor with InMemorySpanExporter used for demonstration purposes
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build();

    // Setup ContextPropagators
    ContextPropagators contextPropagators =
        ContextPropagators.create(
            TextMapPropagator.composite(W3CTraceContextPropagator.getInstance()));

    // Get a Tracer for a scope
    Tracer tracer = tracerProvider.get("org.foo.my-scope");

    // Populate a map with W3C trace context
    Map<String, String> contextCarrier = new HashMap<>();
    SpanContext remoteParentContext =
        SpanContext.createFromRemoteParent(
            IdGenerator.random().generateTraceId(),
            IdGenerator.random().generateSpanId(),
            TraceFlags.getSampled(),
            TraceState.getDefault());
    W3CTraceContextPropagator.getInstance()
        .inject(
            Context.current().with(Span.wrap(remoteParentContext)),
            contextCarrier,
            (carrier, key, value) -> {
              if (carrier != null) {
                carrier.put(key, value);
              }
            });

    // Set parent from the Map<String, String> context carrier
    ((ExtendedSpanBuilder) tracer.spanBuilder("local_root"))
        .setParentFrom(contextPropagators, contextCarrier)
        .startSpan()
        .end();

    // Verify the span has the correct parent context
    assertThat(spanExporter.getFinishedSpanItems())
        .satisfiesExactly(
            span ->
                assertThat(span)
                    .hasName("local_root")
                    .hasParentSpanId(remoteParentContext.getSpanId())
                    .hasTraceId(remoteParentContext.getTraceId()));
  }

  /**
   * Demonstrates {@link ExtendedSpanBuilder#startAndCall(SpanCallable)}, {@link
   * ExtendedSpanBuilder#startAndCall(SpanCallable, BiConsumer)}, {@link
   * ExtendedSpanBuilder#startAndRun(SpanRunnable)}, {@link
   * ExtendedSpanBuilder#startAndRun(SpanRunnable, BiConsumer)}.
   */
  @Test
  void startAndCallOrRun() {
    // Setup SdkTracerProvider
    InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            // Default resource used for demonstration purposes
            .setResource(Resource.getDefault())
            // SimpleSpanProcessor with InMemorySpanExporter used for demonstration purposes
            .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
            .build();

    // Get a Tracer for a scope
    Tracer tracer = tracerProvider.get("org.foo.my-scope");

    // Wrap the resetCheckout method in a span
    String cartId =
        ((ExtendedSpanBuilder) tracer.spanBuilder("reset_checkout_and_return"))
            .startAndCall(() -> resetCheckoutAndReturn("abc123", /* throwException= */ false));
    assertThat(cartId).isEqualTo("abc123");
    // ...or runnable variation
    ((ExtendedSpanBuilder) tracer.spanBuilder("reset_checkout"))
        .startAndRun(() -> resetCheckout("abc123", /* throwException= */ false));

    // Wrap the resetCheckout method in a span; resetCheckout throws an exception
    try {
      ((ExtendedSpanBuilder) tracer.spanBuilder("reset_checkout_and_return"))
          .startAndCall(() -> resetCheckoutAndReturn("def456", /* throwException= */ true));
    } catch (Throwable e) {
      // Ignore expected exception
    }
    // ...or runnable variation
    try {
      ((ExtendedSpanBuilder) tracer.spanBuilder("reset_checkout"))
          .startAndRun(() -> resetCheckout("def456", /* throwException= */ true));
    } catch (Throwable e) {
      // Ignore expected exception
    }

    // Wrap the resetCheckout method in a span; resetCheckout throws an exception; use custom error
    // handler
    try {
      ((ExtendedSpanBuilder) tracer.spanBuilder("reset_checkout_and_return"))
          .startAndCall(
              () -> resetCheckoutAndReturn("ghi789", /* throwException= */ true),
              (span, throwable) -> span.setAttribute("my-attribute", "error"));
    } catch (Throwable e) {
      // Ignore expected exception
    }
    // ...or runnable variation
    try {
      ((ExtendedSpanBuilder) tracer.spanBuilder("reset_checkout"))
          .startAndRun(
              () -> resetCheckout("ghi789", /* throwException= */ true),
              (span, throwable) -> span.setAttribute("my-attribute", "error"));
    } catch (Throwable e) {
      // Ignore expected exception
    }

    // Verify the spans are as expected
    assertThat(spanExporter.getFinishedSpanItems())
        .satisfiesExactly(
            span ->
                assertThat(span)
                    .hasName("reset_checkout_and_return")
                    .hasAttribute(AttributeKey.stringKey("cartId"), "abc123")
                    .hasStatus(StatusData.unset())
                    .hasTotalRecordedEvents(0),
            span ->
                assertThat(span)
                    .hasName("reset_checkout")
                    .hasAttribute(AttributeKey.stringKey("cartId"), "abc123")
                    .hasStatus(StatusData.unset())
                    .hasTotalRecordedEvents(0),
            span ->
                assertThat(span)
                    .hasName("reset_checkout_and_return")
                    .hasAttribute(AttributeKey.stringKey("cartId"), "def456")
                    .hasStatus(StatusData.error())
                    .hasEventsSatisfyingExactly(event -> event.hasName("exception")),
            span ->
                assertThat(span)
                    .hasName("reset_checkout")
                    .hasAttribute(AttributeKey.stringKey("cartId"), "def456")
                    .hasStatus(StatusData.error())
                    .hasEventsSatisfyingExactly(event -> event.hasName("exception")),
            span ->
                assertThat(span)
                    .hasName("reset_checkout_and_return")
                    .hasAttribute(AttributeKey.stringKey("cartId"), "ghi789")
                    .hasAttribute(AttributeKey.stringKey("my-attribute"), "error")
                    .hasStatus(StatusData.unset())
                    .hasTotalRecordedEvents(0),
            span ->
                assertThat(span)
                    .hasName("reset_checkout")
                    .hasAttribute(AttributeKey.stringKey("cartId"), "ghi789")
                    .hasAttribute(AttributeKey.stringKey("my-attribute"), "error")
                    .hasStatus(StatusData.unset())
                    .hasTotalRecordedEvents(0));
  }

  private static String resetCheckoutAndReturn(String cartId, boolean throwException) {
    Span.current().setAttribute("cartId", cartId);
    if (throwException) {
      throw new RuntimeException("Error!");
    }
    return cartId;
  }

  private static void resetCheckout(String cartId, boolean throwException) {
    Span.current().setAttribute("cartId", cartId);
    if (throwException) {
      throw new RuntimeException("Error!");
    }
  }
}
