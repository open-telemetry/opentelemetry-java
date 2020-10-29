/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.inmemory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InMemoryTracing}. */
class InMemoryTracingTest {
  private final TracerSdkProvider tracerSdkProvider = TracerSdkProvider.builder().build();
  private final InMemoryTracing tracing =
      InMemoryTracing.builder().setTracerSdkManagement(tracerSdkProvider).build();
  private final Tracer tracer = tracerSdkProvider.get("InMemoryTracing");

  @Test
  void defaultInstance() {
    assertThat(tracing.getTracerSdkManagement()).isSameAs(tracerSdkProvider);
    assertThat(tracing.getSpanExporter().getFinishedSpanItems()).hasSize(0);
  }

  @Test
  void ctor_nullTracer() {
    assertThrows(
        NullPointerException.class,
        () -> InMemoryTracing.builder().setTracerSdkManagement(null).build(),
        "tracerProvider");
  }

  @Test
  void getFinishedSpanItems() {
    tracer.spanBuilder("A").startSpan().end();
    tracer.spanBuilder("B").startSpan().end();

    List<SpanData> finishedSpanItems = tracing.getSpanExporter().getFinishedSpanItems();
    assertThat(finishedSpanItems).hasSize(2);
    assertThat(finishedSpanItems.get(0).getName()).isEqualTo("A");
    assertThat(finishedSpanItems.get(1).getName()).isEqualTo("B");
  }

  @Test
  void getFinishedSpanItems_sampled() {
    tracer.spanBuilder("A").startSpan().end();
    TraceConfig originalConfig = tracerSdkProvider.getActiveTraceConfig();
    tracerSdkProvider.updateActiveTraceConfig(
        originalConfig.toBuilder().setSampler(Sampler.alwaysOff()).build());
    try {
      tracer.spanBuilder("B").startSpan().end();
    } finally {
      tracerSdkProvider.updateActiveTraceConfig(originalConfig);
    }

    List<SpanData> finishedSpanItems = tracing.getSpanExporter().getFinishedSpanItems();
    assertThat(finishedSpanItems).hasSize(1);
    assertThat(finishedSpanItems.get(0).getName()).isEqualTo("A");
  }

  @Test
  void reset() {
    tracer.spanBuilder("A").startSpan().end();
    tracer.spanBuilder("B").startSpan().end();
    assertThat(tracing.getSpanExporter().getFinishedSpanItems()).hasSize(2);

    tracing.getSpanExporter().reset();
    assertThat(tracing.getSpanExporter().getFinishedSpanItems()).hasSize(0);
  }
}
