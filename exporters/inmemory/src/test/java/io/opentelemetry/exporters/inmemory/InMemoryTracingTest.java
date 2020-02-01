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

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TracerSdkRegistry;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Tracer;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link InMemoryTracing}. */
@RunWith(JUnit4.class)
public class InMemoryTracingTest {
  private final InMemoryTracing tracing = new InMemoryTracing();
  private final Tracer tracer = tracing.getTracerRegistry().get("InMemoryTracing");

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultFinishedSpanItems() {
    assertThat(tracing.getFinishedSpanItems().size()).isEqualTo(0);
  }

  @Test
  public void ctor_tracer() {
    TracerSdkRegistry tracerSdkFactory = TracerSdkRegistry.builder().build();
    InMemoryTracing tracing = new InMemoryTracing(tracerSdkFactory);
    assertThat(tracing.getTracerRegistry()).isSameInstanceAs(tracerSdkFactory);
  }

  @Test
  public void ctor_nullTracer() {
    thrown.expect(NullPointerException.class);
    new InMemoryTracing(null);
  }

  @Test
  public void getFinishedSpanItems() {
    tracer.spanBuilder("A").startSpan().end();
    tracer.spanBuilder("B").startSpan().end();

    List<SpanData> finishedSpanItems = tracing.getFinishedSpanItems();
    assertThat(finishedSpanItems.get(0).getName()).isEqualTo("A");
    assertThat(finishedSpanItems.get(1).getName()).isEqualTo("B");
  }

  @Test
  public void getFinishedSpanItems_sampled() {
    tracer.spanBuilder("A").startSpan().end();
    TracerSdkRegistry tracerSdkFactory = tracing.getTracerRegistry();
    TraceConfig originalConfig = tracerSdkFactory.getActiveTraceConfig();
    tracerSdkFactory.updateActiveTraceConfig(
        originalConfig.toBuilder().setSampler(Samplers.alwaysOff()).build());
    try {
      tracer.spanBuilder("B").startSpan().end();
    } finally {
      tracerSdkFactory.updateActiveTraceConfig(originalConfig);
    }

    List<SpanData> finishedSpanItems = tracing.getFinishedSpanItems();
    assertThat(finishedSpanItems.size()).isEqualTo(1);
    assertThat(finishedSpanItems.get(0).getName()).isEqualTo("A");
  }

  @Test
  public void reset() {
    tracer.spanBuilder("A").startSpan();
    tracer.spanBuilder("B").startSpan();
    tracing.reset();
    assertThat(tracing.getFinishedSpanItems().size()).isEqualTo(0);

    tracing.reset();
    assertThat(tracing.getFinishedSpanItems().size()).isEqualTo(0);
  }
}
