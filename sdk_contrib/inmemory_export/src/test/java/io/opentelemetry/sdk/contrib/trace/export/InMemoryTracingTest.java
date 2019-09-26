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

package io.opentelemetry.sdk.contrib.trace.export;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.trace.util.Samplers;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link InMemoryTracing}. */
@RunWith(JUnit4.class)
public class InMemoryTracingTest {
  InMemoryTracing tracing = new InMemoryTracing();

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultTracer() {
    assertThat(tracing.getTracer()).isInstanceOf(TracerSdk.class);
  }

  @Test
  public void defaultFinishedSpanItems() {
    assertThat(tracing.getFinishedSpanItems().size()).isEqualTo(0);
  }

  @Test
  public void ctor_tracer() {
    TracerSdk tracer = new TracerSdk();
    InMemoryTracing tracing = new InMemoryTracing(tracer);
    assertThat(tracing.getTracer()).isSameInstanceAs(tracer);
  }

  @Test
  public void ctor_nullTracer() {
    thrown.expect(NullPointerException.class);
    new InMemoryTracing(null);
  }

  @Test
  public void getFinishedSpanItems() {
    tracing.getTracer().spanBuilder("A").startSpan().end();
    tracing.getTracer().spanBuilder("B").startSpan().end();

    List<SpanData> finishedSpanItems = tracing.getFinishedSpanItems();
    assertThat(finishedSpanItems.get(0).getName()).isEqualTo("A");
    assertThat(finishedSpanItems.get(1).getName()).isEqualTo("B");
  }

  @Test
  public void getFinishedSpanItems_sampled() {
    tracing.getTracer().spanBuilder("A").startSpan().end();
    tracing.getTracer().spanBuilder("B").setSampler(Samplers.neverSample()).startSpan().end();

    List<SpanData> finishedSpanItems = tracing.getFinishedSpanItems();
    assertThat(finishedSpanItems.size()).isEqualTo(1);
    assertThat(finishedSpanItems.get(0).getName()).isEqualTo("A");
  }

  @Test
  public void reset() {
    tracing.getTracer().spanBuilder("A").startSpan();
    tracing.getTracer().spanBuilder("B").startSpan();
    tracing.reset();
    assertThat(tracing.getFinishedSpanItems().size()).isEqualTo(0);

    tracing.reset();
    assertThat(tracing.getFinishedSpanItems().size()).isEqualTo(0);
  }
}
