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

package io.opentelemetry.opentracingshim.testbed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.opentelemetry.distributedcontext.DefaultDistributedContextManager;
import io.opentelemetry.opentracingshim.TraceShim;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.export.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpansProcessor;
import io.opentelemetry.trace.DefaultSpan;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class OpenTelemetryInteroperabilityTest {
  private final TracerSdk sdk = new TracerSdk();
  private final InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
  private final Tracer otTracer =
      TraceShim.createTracerShim(sdk, DefaultDistributedContextManager.getInstance());

  {
    sdk.addSpanProcessor(SimpleSpansProcessor.newBuilder(spanExporter).build());
  }

  @Before
  public void before() {
    spanExporter.reset();
  }

  @Test
  public void sdkContinuesOpenTracingTrace() {
    Span otSpan = otTracer.buildSpan("ot_span").start();
    try (Scope scope = otTracer.scopeManager().activate(otSpan)) {
      sdk.spanBuilder("otel_span").startSpan().end();
    } finally {
      otSpan.finish();
    }
    assertEquals(sdk.getCurrentSpan().getClass(), DefaultSpan.class);
    assertNull(otTracer.activeSpan());

    List<SpanData> finishedSpans = spanExporter.getFinishedSpanItems();
    assertEquals(2, finishedSpans.size());
    TestUtils.assertSameTrace(finishedSpans);
  }

  @Test
  public void openTracingContinuesSdkTrace() {
    io.opentelemetry.trace.Span otelSpan = sdk.spanBuilder("otel_span").startSpan();
    try (io.opentelemetry.context.Scope scope = sdk.withSpan(otelSpan)) {
      otTracer.buildSpan("ot_span").start().finish();
    } finally {
      otelSpan.end();
    }

    assertEquals(sdk.getCurrentSpan().getClass(), DefaultSpan.class);
    assertNull(otTracer.activeSpan());

    List<SpanData> finishedSpans = spanExporter.getFinishedSpanItems();
    assertEquals(2, finishedSpans.size());
    TestUtils.assertSameTrace(finishedSpans);
  }
}
