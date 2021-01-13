/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InMemorySpanExporter}. */
class InMemorySpanExporterTest {
  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();

  private SdkTracerProvider tracerProvider;
  private Tracer tracer;

  @BeforeEach
  void setup() {
    tracerProvider =
        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
    tracer = tracerProvider.get("InMemorySpanExporterTest");
  }

  @AfterEach
  void tearDown() {
    tracerProvider.shutdown();
  }

  @Test
  void getFinishedSpanItems() {
    tracer.spanBuilder("one").startSpan().end();
    tracer.spanBuilder("two").startSpan().end();
    tracer.spanBuilder("three").startSpan().end();

    List<SpanData> spanItems = exporter.getFinishedSpanItems();
    assertThat(spanItems).isNotNull();
    assertThat(spanItems.size()).isEqualTo(3);
    assertThat(spanItems.get(0).getName()).isEqualTo("one");
    assertThat(spanItems.get(1).getName()).isEqualTo("two");
    assertThat(spanItems.get(2).getName()).isEqualTo("three");
  }

  @Test
  void reset() {
    tracer.spanBuilder("one").startSpan().end();
    tracer.spanBuilder("two").startSpan().end();
    tracer.spanBuilder("three").startSpan().end();
    List<SpanData> spanItems = exporter.getFinishedSpanItems();
    assertThat(spanItems).isNotNull();
    assertThat(spanItems.size()).isEqualTo(3);
    // Reset then expect no items in memory.
    exporter.reset();
    assertThat(exporter.getFinishedSpanItems()).isEmpty();
  }

  @Test
  void shutdown() {
    tracer.spanBuilder("one").startSpan().end();
    tracer.spanBuilder("two").startSpan().end();
    tracer.spanBuilder("three").startSpan().end();
    List<SpanData> spanItems = exporter.getFinishedSpanItems();
    assertThat(spanItems).isNotNull();
    assertThat(spanItems.size()).isEqualTo(3);
    // Shutdown then expect no items in memory.
    exporter.shutdown();
    assertThat(exporter.getFinishedSpanItems()).isEmpty();
    // Cannot add new elements after the shutdown.
    tracer.spanBuilder("one").startSpan().end();
    assertThat(exporter.getFinishedSpanItems()).isEmpty();
  }

  @Test
  void export_ReturnCode() {
    assertThat(exporter.export(Collections.singletonList(makeBasicSpan())).isSuccess()).isTrue();
    exporter.shutdown();
    // After shutdown no more export.
    assertThat(exporter.export(Collections.singletonList(makeBasicSpan())).isSuccess()).isFalse();
    exporter.reset();
    // Reset does not do anything if already shutdown.
    assertThat(exporter.export(Collections.singletonList(makeBasicSpan())).isSuccess()).isFalse();
  }

  static SpanData makeBasicSpan() {
    return TestSpanData.builder()
        .setHasEnded(true)
        .setTraceId(TraceId.getInvalid())
        .setSpanId(SpanId.getInvalid())
        .setName("span")
        .setKind(Kind.SERVER)
        .setStartEpochNanos(100_000_000_100L)
        .setStatus(StatusData.ok())
        .setEndEpochNanos(200_000_000_200L)
        .setTotalRecordedLinks(0)
        .setTotalRecordedEvents(0)
        .build();
  }
}
