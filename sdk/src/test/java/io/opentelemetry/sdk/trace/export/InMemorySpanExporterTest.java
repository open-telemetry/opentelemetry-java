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

package io.opentelemetry.sdk.trace.export;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.trace.TracerSdk;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link InMemorySpanExporter}. */
@RunWith(JUnit4.class)
public class InMemorySpanExporterTest {
  private final TracerSdk tracer = new TracerSdk();
  private final InMemorySpanExporter exporter = InMemorySpanExporter.create();

  @Before
  public void setup() {
    tracer.addSpanProcessor(SimpleSampledSpansProcessor.newBuilder(exporter).build());
  }

  @Test
  public void getFinishedSpanDataItems() {
    tracer.spanBuilder("one").startSpan().end();
    tracer.spanBuilder("two").startSpan().end();
    tracer.spanBuilder("three").startSpan().end();

    List<io.opentelemetry.proto.trace.v1.Span> spanItems = exporter.getFinishedSpanItems();
    assertThat(spanItems).isNotNull();
    assertThat(spanItems.size()).isEqualTo(3);
    assertThat(spanItems.get(0).getName()).isEqualTo("one");
    assertThat(spanItems.get(1).getName()).isEqualTo("two");
    assertThat(spanItems.get(2).getName()).isEqualTo("three");
  }

  @Test
  public void reset() {
    tracer.spanBuilder("one").startSpan().end();
    tracer.spanBuilder("two").startSpan().end();
    tracer.spanBuilder("three").startSpan().end();
    List<io.opentelemetry.proto.trace.v1.Span> spanItems = exporter.getFinishedSpanItems();
    assertThat(spanItems).isNotNull();
    assertThat(spanItems.size()).isEqualTo(3);
    // Reset then expect no items in memory.
    exporter.reset();
    assertThat(exporter.getFinishedSpanItems()).isEmpty();
  }

  @Test
  public void shutdown() {
    tracer.spanBuilder("one").startSpan().end();
    tracer.spanBuilder("two").startSpan().end();
    tracer.spanBuilder("three").startSpan().end();
    List<io.opentelemetry.proto.trace.v1.Span> spanItems = exporter.getFinishedSpanItems();
    assertThat(spanItems).isNotNull();
    assertThat(spanItems.size()).isEqualTo(3);
    // Shutdown then expect no items in memory.
    exporter.shutdown();
    assertThat(exporter.getFinishedSpanItems()).isEmpty();
    // Cannot add new elements after the shutdown.
    tracer.spanBuilder("one").startSpan().end();
    assertThat(exporter.getFinishedSpanItems()).isEmpty();
  }
}
