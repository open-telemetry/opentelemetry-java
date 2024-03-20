/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.sdk.common.ScopeConfig.applyToMatching;
import static io.opentelemetry.sdk.common.ScopeConfig.scopeNameEquals;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;

class TracerConfigTest {

  @Test
  void disableScopes() throws InterruptedException {
    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            // Disable tracerB. Since tracers are enabled by default, tracerA and tracerC are
            // enabled.
            .setTracerConfigProvider(
                applyToMatching(scopeNameEquals("scopeB"), TracerConfig.disabled()))
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build();

    Tracer tracerA = tracerProvider.get("tracerA");
    Tracer tracerB = tracerProvider.get("tracerB");
    Tracer tracerC = tracerProvider.get("tracerC");

    Span parent;
    Span child;
    Span grandchild;

    parent = tracerA.spanBuilder("parent").startSpan();
    try (Scope parentScope = parent.makeCurrent()) {
      parent.setAttribute("a", "1");
      child = tracerB.spanBuilder("child").startSpan();
      // tracerB is disabled and should behave the same as noop tracer
      assertThat(child.getSpanContext()).isEqualTo(parent.getSpanContext());
      assertThat(child.isRecording()).isFalse();
      try (Scope childScope = child.makeCurrent()) {
        child.setAttribute("b", "1");
        grandchild = tracerC.spanBuilder("grandchild").startSpan();
        try (Scope grandchildScope = grandchild.makeCurrent()) {
          grandchild.setAttribute("c", "1");
          Thread.sleep(100);
        } finally {
          grandchild.end();
        }
      } finally {
        child.end();
      }
    } finally {
      parent.end();
    }

    // Only contain tracerA:parent and tracerC:child should be seen
    // tracerC:grandchild should list tracerA:parent as its parent
    assertThat(exporter.getFinishedSpanItems())
        .satisfiesExactlyInAnyOrder(
            spanData ->
                assertThat(spanData)
                    .hasInstrumentationScopeInfo(InstrumentationScopeInfo.create("tracerA"))
                    .hasName("parent")
                    .hasSpanId(parent.getSpanContext().getSpanId())
                    .hasParentSpanId(SpanId.getInvalid())
                    .hasAttributes(Attributes.builder().put("a", "1").build()),
            spanData ->
                assertThat(spanData)
                    .hasInstrumentationScopeInfo(InstrumentationScopeInfo.create("tracerC"))
                    .hasName("grandchild")
                    .hasSpanId(grandchild.getSpanContext().getSpanId())
                    .hasParentSpanId(parent.getSpanContext().getSpanId())
                    .hasAttributes(Attributes.builder().put("c", "1").build()));
  }
}
