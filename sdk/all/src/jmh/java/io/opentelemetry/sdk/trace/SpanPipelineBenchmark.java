/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.common.AttributeValue.booleanAttributeValue;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.export.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Status;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class SpanPipelineBenchmark {

  private final TracerSdk tracerSdk = OpenTelemetrySdk.getTracerProvider().get("benchmarkTracer");

  @Setup(Level.Trial)
  public final void setup() {
    SpanExporter exporter = new NoOpSpanExporter();
    OpenTelemetrySdk.getTracerProvider()
        .addSpanProcessor(SimpleSpanProcessor.newBuilder(exporter).build());
  }

  @Benchmark
  @Threads(value = 5)
  @Fork(1)
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 1)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void runThePipeline_05Threads() {
    doWork();
  }

  private void doWork() {
    Span span =
        tracerSdk
            .spanBuilder("benchmarkSpan")
            .setSpanKind(Kind.CLIENT)
            .setAttribute("key", "value")
            .addLink(new TestLink())
            .startSpan();
    span.addEvent("started", Attributes.of("operation", stringAttributeValue("some_work")));
    span.setAttribute("longAttribute", 33L);
    span.setAttribute("stringAttribute", "test_value");
    span.setAttribute("doubleAttribute", 4844.44d);
    span.setAttribute("booleanAttribute", false);
    span.setStatus(Status.OK);

    span.addEvent("testEvent");
    span.addEvent(new TestEvent());
    span.end();
  }

  private static class NoOpSpanExporter implements SpanExporter {
    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public void shutdown() {
      // no-op
    }
  }

  private static class TestLink implements Link {
    @Override
    public SpanContext getContext() {
      return SpanContext.getInvalid();
    }

    @Override
    public Attributes getAttributes() {
      return Attributes.of("linkAttr", stringAttributeValue("linkValue"));
    }
  }

  private static class TestEvent implements Event {
    @Override
    public String getName() {
      return "ended";
    }

    @Override
    public Attributes getAttributes() {
      return Attributes.of("finalized", booleanAttributeValue(true));
    }
  }
}
