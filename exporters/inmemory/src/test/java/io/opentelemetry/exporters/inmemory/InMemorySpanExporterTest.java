/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.inmemory;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.test.TestSpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracer;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InMemorySpanExporter}. */
class InMemorySpanExporterTest {
  private final TracerSdkProvider tracerSdkProvider = TracerSdkProvider.builder().build();
  private final Tracer tracer = tracerSdkProvider.get("InMemorySpanExporterTest");
  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();

  @BeforeEach
  void setup() {
    tracerSdkProvider.addSpanProcessor(SimpleSpanProcessor.newBuilder(exporter).build());
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
    assertThat(exporter.export(Collections.singletonList(makeBasicSpan())))
        .isEqualTo(ResultCode.SUCCESS);
    exporter.shutdown();
    // After shutdown no more export.
    assertThat(exporter.export(Collections.singletonList(makeBasicSpan())))
        .isEqualTo(ResultCode.FAILURE);
    exporter.reset();
    // Reset does not do anything if already shutdown.
    assertThat(exporter.export(Collections.singletonList(makeBasicSpan())))
        .isEqualTo(ResultCode.FAILURE);
  }

  static SpanData makeBasicSpan() {
    return TestSpanData.newBuilder()
        .setHasEnded(true)
        .setTraceId(TraceId.getInvalid())
        .setSpanId(SpanId.getInvalid())
        .setName("span")
        .setKind(Kind.SERVER)
        .setStartEpochNanos(100_000_000_100L)
        .setStatus(Status.OK)
        .setEndEpochNanos(200_000_000_200L)
        .setTotalRecordedLinks(0)
        .setTotalRecordedEvents(0)
        .build();
  }
}
