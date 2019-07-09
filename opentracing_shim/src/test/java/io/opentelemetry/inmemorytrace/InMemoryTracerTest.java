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

package io.opentelemetry.inmemorytrace;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.SpanData;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link InMemoryTracer}. */
@RunWith(JUnit4.class)
public class InMemoryTracerTest {
  @Test
  public void defaultCtor() {
    InMemoryTracer tracer = new InMemoryTracer();
    assertThat(tracer.getCurrentSpan()).isEqualTo(DefaultSpan.getInvalid());
    assertThat(tracer.spanBuilder("one")).isNotNull();
    assertThat(tracer.getResource()).isNotNull();
    assertThat(tracer.getHttpTextFormat()).isNotNull();
    assertThat(tracer.getFinishedSpanDataItems()).isNotNull();
    assertThat(tracer.getFinishedSpanDataItems().size()).isEqualTo(0);
  }

  @Test
  public void getFinishedSpanDataItems() {
    InMemoryTracer tracer = new InMemoryTracer();
    tracer.spanBuilder("one").startSpan().end();
    tracer.spanBuilder("two").startSpan().end();
    tracer.spanBuilder("three").startSpan().end();

    List<SpanData> spanItems = tracer.getFinishedSpanDataItems();
    assertThat(spanItems).isNotNull();
    assertThat(spanItems.size()).isEqualTo(3);
    assertThat(spanItems.get(0).getName()).isEqualTo("one");
    assertThat(spanItems.get(1).getName()).isEqualTo("two");
    assertThat(spanItems.get(2).getName()).isEqualTo("three");
  }

  @Test
  public void reset() {
    InMemoryTracer tracer = new InMemoryTracer();
    tracer.spanBuilder("one").startSpan().end();
    tracer.spanBuilder("two").startSpan().end();
    tracer.spanBuilder("three").startSpan().end();
    tracer.reset();

    List<SpanData> spanItems = tracer.getFinishedSpanDataItems();
    assertThat(spanItems).isNotNull();
    assertThat(spanItems.size()).isEqualTo(0);
  }
}
