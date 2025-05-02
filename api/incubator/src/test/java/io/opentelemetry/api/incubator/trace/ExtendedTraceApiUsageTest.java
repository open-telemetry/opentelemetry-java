/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.trace.internal.TracerConfig.disabled;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
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
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of extended Trace API. */
class ExtendedTraceApiUsageTest {

  @Test
  void tracerEnabled() {
    // Setup SdkTracerProvider
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    SdkTracerProviderBuilder tracerProviderBuilder =
        SdkTracerProvider.builder()
            // Default resource used for demonstration purposes
            .setResource(Resource.getDefault())
            // In-memory exporter used for demonstration purposes
            .addSpanProcessor(SimpleSpanProcessor.create(exporter));
    // Disable tracerB
    SdkTracerProviderUtil.addTracerConfiguratorCondition(
        tracerProviderBuilder, nameEquals("tracerB"), disabled());
    SdkTracerProvider tracerProvider = tracerProviderBuilder.build();

    // Create tracerA and tracerB
    ExtendedTracer tracerA = (ExtendedTracer) tracerProvider.get("tracerA");
    ExtendedTracer tracerB = (ExtendedTracer) tracerProvider.get("tracerB");

    // Check if tracer is enabled before recording span and avoid unnecessary computation
    if (tracerA.isEnabled()) {
      tracerA
          .spanBuilder("span name")
          .startSpan()
          .setAllAttributes(Attributes.builder().put("result", flipCoin()).build())
          .end();
    }
    if (tracerB.isEnabled()) {
      tracerB
          .spanBuilder("span name")
          .startSpan()
          .setAllAttributes(Attributes.builder().put("result", flipCoin()).build())
          .end();
    }

    // tracerA is enabled, tracerB is disabled
    assertThat(tracerA.isEnabled()).isTrue();
    assertThat(tracerB.isEnabled()).isFalse();

    // Collected data only consists of spans from tracerA. Note, tracerB's spans would be
    // omitted from the results even if spans were recorded. The check if enabled simply avoids
    // unnecessary computation.
    assertThat(exporter.getFinishedSpanItems())
        .allSatisfy(
            spanData ->
                assertThat(spanData.getInstrumentationScopeInfo().getName()).isEqualTo("tracerA"));
  }

  private static final Random random = new Random();

  private static String flipCoin() {
    return random.nextBoolean() ? "heads" : "tails";
  }

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
    ExtendedTracer extendedTracer = (ExtendedTracer) tracer;

    // Wrap the resetCheckout method in a span
    String cartId =
        ((ExtendedSpanBuilder) tracer.spanBuilder("reset_checkout_and_return"))
            .setAttribute("key123", "val456")
            .startAndCall(() -> resetCheckoutAndReturn("abc123", /* throwException= */ false));
    assertThat(cartId).isEqualTo("abc123");
    // ...or use ExtendedTracer instance
    // ...or runnable variation
    extendedTracer
        .spanBuilder("reset_checkout")
        .startAndRun(() -> resetCheckout("abc123", /* throwException= */ false));

    // Wrap the resetCheckout method in a span; resetCheckout throws an exception
    try {
      extendedTracer
          .spanBuilder("reset_checkout_and_return")
          .startAndCall(() -> resetCheckoutAndReturn("def456", /* throwException= */ true));
    } catch (Throwable e) {
      // Ignore expected exception
    }
    // ...or runnable variation
    try {
      extendedTracer
          .spanBuilder("reset_checkout")
          .startAndRun(() -> resetCheckout("def456", /* throwException= */ true));
    } catch (Throwable e) {
      // Ignore expected exception
    }

    // Wrap the resetCheckout method in a span; resetCheckout throws an exception; use custom error
    // handler
    try {
      extendedTracer
          .spanBuilder("reset_checkout_and_return")
          .startAndCall(
              () -> resetCheckoutAndReturn("ghi789", /* throwException= */ true),
              (span, throwable) -> span.setAttribute("my-attribute", "error"));
    } catch (Throwable e) {
      // Ignore expected exception
    }
    // ...or runnable variation
    try {
      extendedTracer
          .spanBuilder("reset_checkout")
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
