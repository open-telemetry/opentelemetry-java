/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Demonstrating usage of extended ContextPropagators API. */
class ExtendedContextPropagatorsUsageTest {

  @Test
  void getTextMapPropagationContextUsage() {
    // Setup Propagators
    ContextPropagators propagators =
        ContextPropagators.create(
            TextMapPropagator.composite(W3CTraceContextPropagator.getInstance()));

    // Setup SdkTracerProvider
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder().setSampler(Sampler.alwaysOn()).build();

    // Get a Tracer for a scope
    Tracer tracer = tracerProvider.get("org.foo.my-scope");

    try (Scope scope = tracer.spanBuilder("span name").startSpan().makeCurrent()) {
      // Simplify context injection by getting a text map of the key/value pairs to inject
      Map<String, String> textMap =
          ExtendedContextPropagators.getTextMapPropagationContext(propagators);
      // Assert textmap contains the "traceparent" field as injected by W3CTraceContextPropagator
      assertThat(textMap)
          .hasEntrySatisfying("traceparent", value -> assertThat(value).isNotEmpty());
    }
  }

  @Test
  void extractTextMapPropagationContextUsage() {
    // Setup Propagators
    ContextPropagators propagators =
        ContextPropagators.create(
            TextMapPropagator.composite(W3CTraceContextPropagator.getInstance()));

    // Setup map with context key/value pairs
    Map<String, String> contextCarrier =
        ImmutableMap.of("traceparent", "00-713bde54561be5ded62545d0e7369d4a-3c3a5ddefce9c1e1-01");

    // Extract context from the carrier map
    Context context =
        ExtendedContextPropagators.extractTextMapPropagationContext(contextCarrier, propagators);
    // Assert SpanContext is properly extracted from the W3cTraceContextPropagator
    assertThat(Span.fromContext(context).getSpanContext())
        .isEqualTo(
            SpanContext.createFromRemoteParent(
                "713bde54561be5ded62545d0e7369d4a",
                "3c3a5ddefce9c1e1",
                TraceFlags.getSampled(),
                TraceState.getDefault()));
  }
}
